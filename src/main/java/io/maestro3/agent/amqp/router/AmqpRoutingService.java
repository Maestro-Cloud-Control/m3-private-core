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

package io.maestro3.agent.amqp.router;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AmqpRoutingService implements IAmqpRoutingService {

    private List<String> notificationTopics;
    private String notificationQueueName;

    private String openStackNotificationsTopic;
    private String openStackNovaNotificationsQueue;
    private String openStackCinderNotificationsQueue;
    private String openStackGlanceNotificationsQueue;

    private String openStackNovaNotificationsExchange;
    private String openStackCinderNotificationsExchange;
    private String openStackGlanceNotificationsExchange;

    @Autowired
    public AmqpRoutingService(@Value("${notification.topics}") List<String> topics,
                              @Value("${openstack.notifications.topic:default}") String openStackNotificationsTopic,
                              @Value("${openstack.nova.notifications.exchange:default}") String openStackNovaNotificationsExchange,
                              @Value("${openstack.cinder.notifications.exchange:default}") String openStackCinderNotificationsExchange,
                              @Value("${openstack.glance.notifications.exchange:default}") String openStackGlanceNotificationsExchange,
                              @Value("${openstack.nova.notifications.queue:default}") String openStackNovaNotificationsQueue,
                              @Value("${openstack.cinder.notifications.queue:default}") String openStackCinderNotificationsQueue,
                              @Value("${openstack.glance.notifications.queue:default}") String openStackGlanceNotificationsQueue,
                              @Value("${notification.queue.name}") String notificationQueueName) {
        this.notificationTopics = topics;
        this.notificationQueueName = notificationQueueName;
        this.openStackNovaNotificationsExchange = openStackNovaNotificationsExchange;
        this.openStackCinderNotificationsExchange = openStackCinderNotificationsExchange;
        this.openStackNovaNotificationsQueue = openStackNovaNotificationsQueue;
        this.openStackGlanceNotificationsExchange = openStackGlanceNotificationsExchange;
        this.openStackCinderNotificationsQueue = openStackCinderNotificationsQueue;
        this.openStackGlanceNotificationsQueue = openStackGlanceNotificationsQueue;
        this.openStackNotificationsTopic = openStackNotificationsTopic;
    }

    @Override
    public String getOpenStackNovaNotificationsQueue() {
        return openStackNovaNotificationsQueue;
    }

    @Override
    public String getOpenStackCinderNotificationsQueue() {
        return openStackCinderNotificationsQueue;
    }

    @Override
    public String getOpenStackGlanceNotificationsQueue() {
        return openStackGlanceNotificationsQueue;
    }

    @Override
    public String getOsNotificationsTopic() {
        return openStackNotificationsTopic;
    }

    @Override
    public String getOpenStackNovaNotificationsExchange() {
        return openStackNovaNotificationsExchange;
    }

    @Override
    public String getOpenStackCinderNotificationsExchange() {
        return openStackCinderNotificationsExchange;
    }

    @Override
    public String getOpenStackGlanceNotificationsExchange() {
        return openStackGlanceNotificationsExchange;
    }

    @Override
    public String getNotificationQueue() {
        return notificationQueueName;
    }

    @Override
    public List<String> getNotificationTopics() {
        return notificationTopics;
    }
}
