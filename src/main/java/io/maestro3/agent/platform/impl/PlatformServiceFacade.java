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

package io.maestro3.agent.platform.impl;

import io.maestro3.agent.cadf.ICadfAuditEventSender;
import io.maestro3.agent.dao.IPlatformServiceEntryDao;
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.platform.IPlatformServiceFacade;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import io.maestro3.agent.util.CadfUtils;
import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfActions;
import io.maestro3.cadf.model.CadfAttachment;
import io.maestro3.cadf.model.CadfAuditEvent;
import io.maestro3.cadf.model.CadfMeasurement;
import io.maestro3.cadf.model.CadfResource;
import io.maestro3.cadf.model.CadfResourceTypes;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.terraform.integration.model.TemplateStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Serhii Akhmetshin
 * Created: 23/02/2024
 */
@Service
public class PlatformServiceFacade implements IPlatformServiceFacade {

    private final IPlatformServiceEntryDao entryDao;
    private final ICadfAuditEventSender sender;
    private final String agentName;

    @Autowired
    public PlatformServiceFacade(IPlatformServiceEntryDao entryDao,
                                 ICadfAuditEventSender sender,
                                 @Value("${private.agent.name}") String agentName) {
        this.agentName = agentName;
        this.entryDao = entryDao;
        this.sender = sender;
    }

    @Override
    public void deleteEntry(String id) {
        Optional<PlatformServiceEntry> entryOptional = entryDao.findById(id);
        entryOptional.ifPresent(
                entry -> sender.sendCadfAuditEvent(prepareAudit(CadfActions.delete(), entry, null),
                        Collections.singletonList(AuditEventGroupType.PRIVATE_AGENT)));
    }

    @Override
    public void entryStateChanged(String paasId, TemplateStatus newStatus) {
        if (newStatus == TemplateStatus.CREATED || newStatus == TemplateStatus.FAILED_TO_CREATE) {
            Optional<PlatformServiceEntry> entryOptional = entryDao.findById(paasId);
            entryOptional.ifPresent(
                    entry -> sender.sendCadfAuditEvent(prepareAudit(CadfActions.create(), entry, newStatus),
                            Collections.singletonList(AuditEventGroupType.PRIVATE_AGENT)));
        }
    }

    private CadfAuditEvent prepareAudit(ICadfAction action, PlatformServiceEntry entry, TemplateStatus status) {
        String actionId = new ObjectId().toHexString();
        CadfResource target = CadfUtils.createTarget(CadfResourceTypes.data().template().stack(), entry.getServiceEntryId());

        Date date = new Date();
        List<CadfMeasurement> measurements = Collections.emptyList();
        List<CadfAttachment> attachments = resolveServiceAttachments(entry, status);

        return CadfUtils.generateCadfAuditEvent(
                action,
                date,
                actionId,
                target,
                measurements,
                attachments,
                new ArrayList<>(),
                CadfResource.builder()
                        .ofType(CadfResourceTypes.unknown())
                        .withId(CadfUtils.ID_NAMESPACE + agentName)
                        .withName(agentName)
                        .build()
        );
    }

    private List<CadfAttachment> resolveServiceAttachments(PlatformServiceEntry entry, TemplateStatus status) {
        List<CadfAttachment> result = new ArrayList<>();
        result.add(CadfUtils.createAttachment("string", "id", entry.getServiceEntryId()));
        result.add(CadfUtils.createAttachment("string", "cloud", entry.getCloud()));
        result.add(CadfUtils.createAttachment("string", "region", entry.getRegionName()));
        result.add(CadfUtils.createAttachment("string", "tenant", entry.getTenantDisplayName()));
        result.add(CadfUtils.createAttachment("date", "createdDate", new Date().getTime()));
        result.add(CadfUtils.createAttachment("string", "state", getStateString(status)));
        result.add(CadfUtils.createAttachment("string", "info", getInfoString(status)));
        return result;
    }

    private static String getInfoString(TemplateStatus status) {
        return status == null ? "Successfully deleted" :
                status == TemplateStatus.CREATED ? "Successfully created" : "Failed to create";
    }

    private static String getStateString(TemplateStatus status) {
        return status == null ? "DELETED" :
                status == TemplateStatus.CREATED ? "CREATED" : "FAILED";
    }
}
