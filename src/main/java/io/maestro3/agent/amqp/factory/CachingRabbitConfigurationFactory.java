/*
 * Copyright 2023 Maestro Cloud Control LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.maestro3.agent.amqp.factory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.maestro3.agent.amqp.PrivateAgentAmqpConstants;
import io.maestro3.agent.amqp.RegionHeaderMapper;
import io.maestro3.agent.amqp.model.IRabbitConfiguration;
import io.maestro3.agent.amqp.model.SimpleRabbitConfiguration;
import io.maestro3.agent.amqp.tracker.IAmqpMessageTracker;
import com.rabbitmq.client.MetricsCollector;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class CachingRabbitConfigurationFactory implements RabbitConfigurationFactory {

    private static final int CACHE_MAX_SIZE = 10;
    private static final String CACHE_KEY_DELIMITER = "=";

    private final Cache<String, ConnectionFactory> connectionFactoryCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .build();

    private IAmqpMessageTracker messageTracker;

    @Autowired
    public CachingRabbitConfigurationFactory(IAmqpMessageTracker messageTracker) {
        this.messageTracker = messageTracker;
    }

    @Override
    public ConnectionFactory createConnectionFactory(String host, int port, String username, String password, String vhost) {
        return createConnectionFactory(host, port, username, password, vhost, PrivateAgentAmqpConstants.TEMP_ID);
    }

    @Override
    public ConnectionFactory createConnectionFactory(String host, int port, String username, String password,
                                                     String vhost, String regionId) {
        Assert.hasText(host, "Rabbit host can not be null or empty");
        Assert.hasText(username, "Rabbit username can not be null or empty");
        Assert.hasText(password, "Rabbit password can not be null or empty");
        Assert.hasText(vhost, "Rabbit vhost can not be null or empty");

        String cacheKey = createCacheKey(host, port, username, password, vhost);
        ConnectionFactory cachedFactory = connectionFactoryCache.getIfPresent(cacheKey);
        IRabbitConfiguration rabbitConfiguration =
                new SimpleRabbitConfiguration(host, username, password, vhost, port);
        if (cachedFactory != null) {
            messageTracker.registerMetricCollector(regionId, rabbitConfiguration,
                    ((CachingConnectionFactory)cachedFactory).getRabbitConnectionFactory().getMetricsCollector());
            return cachedFactory;
        } else {
            CachingConnectionFactory newFactory = new CachingConnectionFactory(host, port);
            newFactory.setUsername(username);
            newFactory.setPassword(password);
            newFactory.setVirtualHost(vhost);
            if (regionId != null) {
                MetricsCollector metricsCollector = messageTracker.registerMetricCollector(regionId, rabbitConfiguration);
                newFactory.getRabbitConnectionFactory().setMetricsCollector(metricsCollector);
            }
            connectionFactoryCache.put(cacheKey, newFactory);
            return newFactory;
        }
    }

    @Override
    public SimpleMessageListenerContainer createListenerContainer(ConnectionFactory connectionFactory,
                                                                  int minConcurrentConsumers,
                                                                  int maxConcurrentConsumers,
                                                                  int shutdownTimeoutMillis) {
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        listenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
        listenerContainer.setConcurrentConsumers(minConcurrentConsumers);
        listenerContainer.setMaxConcurrentConsumers(maxConcurrentConsumers);
        listenerContainer.setBatchSize(1);
        listenerContainer.setPrefetchCount(1);
        listenerContainer.setShutdownTimeout(shutdownTimeoutMillis);
        listenerContainer.setDefaultRequeueRejected(false);
        return listenerContainer;
    }

    @Override
    public AmqpInboundChannelAdapter createInboundChannelAdapter(AbstractMessageListenerContainer listenerContainer,
                                                                 MessageChannel messageChannel,
                                                                 String regionId) {
        AmqpInboundChannelAdapter inboundChannelAdapter = new AmqpInboundChannelAdapter(listenerContainer);
        inboundChannelAdapter.setOutputChannel(messageChannel);
        inboundChannelAdapter.setHeaderMapper(new RegionHeaderMapper(regionId));
        inboundChannelAdapter.afterPropertiesSet();

        return inboundChannelAdapter;
    }

    private String createCacheKey(String host, int port, String username, String password, String vhost) {
        return host + CACHE_KEY_DELIMITER + port + CACHE_KEY_DELIMITER + username + CACHE_KEY_DELIMITER + password + CACHE_KEY_DELIMITER + vhost;
    }
}
