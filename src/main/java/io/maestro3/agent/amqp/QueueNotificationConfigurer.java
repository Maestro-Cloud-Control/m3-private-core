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

package io.maestro3.agent.amqp;

import io.maestro3.agent.amqp.listener.ListenersLifecycleManager;
import io.maestro3.agent.dao.IRegionRepository;
import io.maestro3.agent.model.base.IAmqpSupportedRegion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Component
public class QueueNotificationConfigurer implements ApplicationListener<ContextRefreshedEvent> {

    private IRegionRepository regionService;
    private ListenersLifecycleManager listenersConfigurationService;

    @Autowired
    public QueueNotificationConfigurer(IRegionRepository regionService,
                                       ListenersLifecycleManager listenersConfigurationService) {
        this.regionService = regionService;
        this.listenersConfigurationService = listenersConfigurationService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (Objects.nonNull(event.getApplicationContext().getParent())) {
            return;
        }

        Collection<IAmqpSupportedRegion> regions = regionService.findAllAmqpRegions();
        Optional.ofNullable(regions)
                .ifPresent(clouds -> clouds.stream()
                        .filter(region -> Objects.nonNull(region.getRabbitNotificationConfig()))
                        .forEach(region -> listenersConfigurationService.listenToNotifications(region)));

        if (!listenersConfigurationService.areListenersRunning()) {
            listenersConfigurationService.startListeners();
        }
    }
}
