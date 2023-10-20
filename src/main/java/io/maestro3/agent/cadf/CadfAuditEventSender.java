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

package io.maestro3.agent.cadf;

import io.maestro3.agent.amqp.PrivateAgentAmqpConstants;
import io.maestro3.agent.amqp.model.SdkRabbitConfiguration;
import io.maestro3.agent.amqp.tracker.IAmqpMessageTracker;
import io.maestro3.sdk.exception.M3SdkException;
import io.maestro3.sdk.v3.client.IM3Client;
import io.maestro3.sdk.v3.core.StaticPrincipal;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
import io.maestro3.sdk.v3.request.audit.SaveCadfEventRequest;
import io.maestro3.cadf.model.CadfAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CadfAuditEventSender implements ICadfAuditEventSender {

    private static final Logger LOG = LoggerFactory.getLogger(CadfAuditEventSender.class);

    private final IM3Client client;
    private final String auditQueue;
    private final IAmqpMessageTracker messageTracker;

    @Autowired
    public CadfAuditEventSender(@Qualifier("m3ServerClient") IM3Client client,
                                SdkRabbitConfiguration configuration,
                                IAmqpMessageTracker messageTracker) {
        this.messageTracker = messageTracker;
        this.auditQueue = configuration.getResponseQueue();
        this.client = client;
    }

    @Override
    public void sendCadfAuditEvent(CadfAuditEvent event, List<AuditEventGroupType> groupTypes) {
        try {
            if (event == null) {
                return;
            }
            SaveCadfEventRequest request = SaveCadfEventRequest.builder()
                    .withEvent(event)
                    .withQualifier(AuditEventGroupType.getQualifier(groupTypes))
                    .build();

            client.auditManager().saveCadfEvent(StaticPrincipal.getPrincipal(), request);
            messageTracker.trackSend(PrivateAgentAmqpConstants.SDK_REGION, auditQueue);
        } catch (M3SdkException e) {
            LOG.error("Failed to publish audit event", e);
        }
    }
}
