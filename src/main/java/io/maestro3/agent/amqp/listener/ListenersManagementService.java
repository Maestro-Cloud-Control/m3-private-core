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

package io.maestro3.agent.amqp.listener;

import com.google.common.collect.Lists;
import io.maestro3.agent.amqp.IntegrationChannels;
import io.maestro3.agent.amqp.factory.RabbitConfigurationFactory;
import io.maestro3.agent.amqp.router.IAmqpRoutingService;
import io.maestro3.agent.model.base.IAmqpSupportedRegion;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.base.RabbitNotificationConfig;
import com.rabbitmq.client.Channel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ListenersManagementService implements ListenersLifecycleManager, IAmqpHelperService {

    private static final Logger LOG = LoggerFactory.getLogger(ListenersManagementService.class);

    private RabbitConfigurationFactory rabbitConfigurationFactory;
    private IAmqpRoutingService routingService;

    private Map<PrivateCloudType, MessageChannel> messageChannels = new HashMap<>();

    private List<IAmqpSupportedRegion> regionsToConfigureNotifications;
    private List<SimpleMessageListenerContainer> notificationsListeners;

    public ListenersManagementService(@Autowired @Qualifier(IntegrationChannels.Inbound.OS_BYTES)
                                          MessageChannel osChannel,
                                      @Autowired RabbitConfigurationFactory rabbitConfigurationFactory,
                                      @Autowired IAmqpRoutingService routingService) {
        this.rabbitConfigurationFactory = rabbitConfigurationFactory;
        this.messageChannels.put(PrivateCloudType.OPEN_STACK, osChannel);
        this.routingService = routingService;

        regionsToConfigureNotifications = Lists.newLinkedList();
        notificationsListeners = Lists.newLinkedList();
    }

    @Override
    public void startListeners() {
        cleanListenersQueueNames(notificationsListeners);
        configureNotificationQueues();
        startListeners(notificationsListeners);
    }

    @Override
    public void listenToNotifications(IAmqpSupportedRegion amqpSupportedRegion) {
        Assert.notNull(amqpSupportedRegion, "amqpSupportedRegion must not be null");
        Assert.notNull(amqpSupportedRegion.getRabbitNotificationConfig(), "notificationConfig must not be null");
        regionsToConfigureNotifications.add(amqpSupportedRegion);
    }

    @Override
    public boolean areListenersRunning() {
        return areListenersRunning(notificationsListeners);
    }

    @Override
    public void assertExchangeExist(RabbitNotificationConfig config) throws IllegalStateException {
        try {
            ConnectionFactory connectionFactory = rabbitConfigurationFactory.createConnectionFactory(
                config.getRabbitHost(),
                config.getRabbitPort(),
                config.getRabbitUsername(),
                config.getRabbitPassword(),
                config.getRabbitVirtHost());
            try (Connection connection = connectionFactory.createConnection()) {
                for (String exchange : config.getQueueMapping().keySet()) {
                    try (Channel channel = connection.createChannel(false)) {
                        channel.exchangeDeclarePassive(exchange);
                    }
                }
            }
        } catch (Exception ex) {
            Throwable cause = ex;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            throw new IllegalStateException("ERROR: Illegal rabbit configuration. " + cause.getMessage());
        }
    }

    @Override
    public boolean stopListeners() {
        return stopListeners(notificationsListeners);
    }

    private void configureNotificationQueues() {
        if (CollectionUtils.isEmpty(regionsToConfigureNotifications)) {
            return;
        }

        for (IAmqpSupportedRegion region : regionsToConfigureNotifications) {
            RabbitNotificationConfig config = region.getRabbitNotificationConfig();
            ConnectionFactory connectionFactory = rabbitConfigurationFactory.createConnectionFactory(
                config.getRabbitHost(), config.getRabbitPort(),
                config.getRabbitUsername(), config.getRabbitPassword(), config.getRabbitVirtHost(), region.getId());
            Map<String, List<String>> queueMapping = config.getQueueMapping();
            if (MapUtils.isNotEmpty(queueMapping)) {
                queueMapping.forEach((exchange, queues) -> {
                    for (String queue : queues) {

                        ensureOsNotificationQueuesBindings(
                            queue,
                            exchange,
                            connectionFactory);

                        SimpleMessageListenerContainer listenerContainer = rabbitConfigurationFactory.createListenerContainer(
                            connectionFactory,
                            config.getMinConcurrentConsumers(),
                            config.getMaxConcurrentConsumers(),
                            config.getShutdownTimeoutMillis());

                        listenerContainer.addQueueNames(queue);
                        notificationsListeners.add(listenerContainer);

                        MessageChannel messageChannel = messageChannels.get(region.getCloud());
                        AmqpInboundChannelAdapter inboundChannelAdapter = rabbitConfigurationFactory.createInboundChannelAdapter(
                            listenerContainer, messageChannel, region.getId());
                        inboundChannelAdapter.start();
                    }
                });
            }
        }
    }

    private void ensureOsNotificationQueuesBindings(String notificationsQueue,
                                                    String exchange,
                                                    ConnectionFactory connectionFactory) {
        try {
            AmqpAdmin amqpAdmin = new RabbitAdmin(connectionFactory);
            ensureOsNotificQueueBinding(
                notificationsQueue,
                Optional.ofNullable(exchange).orElse(routingService.getNotificationQueue()),
                routingService.getNotificationTopics(),
                amqpAdmin);
        } catch (Exception e) {
            LOG.error("Error where occurred due Queue/Binding refresh push notifications. " + e.getMessage(), e);
        }
    }

    private void ensureOsNotificQueueBinding(String notificationsQueue,
                                             String exchangeKey,
                                             List<String> routingKeys,
                                             AmqpAdmin amqpAdmin) {

        Queue queue = new Queue(notificationsQueue, true, false, false);
        DirectExchange directExchange = new DirectExchange(exchangeKey, true, false);
        QueueInformation queueInformation = amqpAdmin.getQueueInfo(notificationsQueue);
        if (queueInformation == null) {
            amqpAdmin.declareQueue(queue);
        }
        for (String routingKey : routingKeys) {
            amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(directExchange).with(routingKey));
        }
    }

    private void startListeners(Collection<SimpleMessageListenerContainer> listeners) {
        for (SimpleMessageListenerContainer listener : listeners) {
            String[] queueNames = listener.getQueueNames();
            if (!ArrayUtils.isEmpty(queueNames)) {
                listener.start();
                LOG.info("{} will listen for following queues: {}", listener, ArrayUtils.toString(queueNames));
            }
        }
    }

    private boolean areListenersRunning(Collection<SimpleMessageListenerContainer> listeners) {
        for (SimpleMessageListenerContainer listener : listeners) {
            if (listener.isActive()) {
                return true;
            }
        }
        return false;
    }


    private boolean stopListeners(Collection<SimpleMessageListenerContainer> listeners) {
        LOG.info("Stopping AMQP listeners...");
        if (areListenersRunning(listeners)) {
            for (SimpleMessageListenerContainer listener : listeners) {
                listener.start();
                LOG.info("{} listeners have been successfully stopped.", listener);
            }
            return true;
        } else {
            LOG.info("AMQP listeners are currently stopped.");
            return false;
        }
    }

    private void cleanListenersQueueNames(Collection<SimpleMessageListenerContainer> listeners) {
        for (SimpleMessageListenerContainer listener : listeners) {
            listener.setQueueNames();
        }
    }
}
