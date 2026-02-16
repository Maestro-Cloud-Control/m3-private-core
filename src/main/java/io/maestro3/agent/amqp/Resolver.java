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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Resolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Resolver.class);

    private Map<String, IResourceUpdater> resourceUpdatersMap;

    @Autowired
    public Resolver(List<IResourceUpdater> resourceUpdaters) {
        this.resourceUpdatersMap = new HashMap<>();
        for (IResourceUpdater resourceUpdater : resourceUpdaters) {
            for (String resourceType : resourceUpdater.getResourceTypes()) {
                resourceUpdatersMap.put(resourceType, resourceUpdater);
            }
        }
    }

    @ServiceActivator(inputChannel = IntegrationChannels.Inbound.VMWARE_DESCRIBER, autoStartup = "true")
    private void checkAudit(Message<VMWareEvent> message) {
        VMWareEvent event = message.getPayload();
        IResourceUpdater updater = resourceUpdatersMap.get(event.getEntityType());
        if (updater == null) {
            LOGGER.trace("Event with resource type {} is not supported by application", event.getEntityType());
            return;
        }
        updater.addToQueue(event);
    }
}
