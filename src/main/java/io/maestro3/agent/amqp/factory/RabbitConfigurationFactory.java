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

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.messaging.MessageChannel;

public interface RabbitConfigurationFactory {

    ConnectionFactory createConnectionFactory(String host, int port, String username, String password, String vhost, String regionId);

    ConnectionFactory createConnectionFactory(String host, int port, String username, String password, String vhost);

    SimpleMessageListenerContainer createListenerContainer(ConnectionFactory connectionFactory, int minConcurrentConsumers, int maxConcurrentConsumers, int shutdownTimeoutMillis);

    AmqpInboundChannelAdapter createInboundChannelAdapter(AbstractMessageListenerContainer listenerContainer,
                                                          MessageChannel messageChannel,
                                                          String regionId);
}
