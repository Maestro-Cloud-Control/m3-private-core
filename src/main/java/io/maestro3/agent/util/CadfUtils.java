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

package io.maestro3.agent.util;

import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfAttachment;
import io.maestro3.cadf.model.CadfAuditEvent;
import io.maestro3.cadf.model.CadfEventType;
import io.maestro3.cadf.model.CadfMeasurement;
import io.maestro3.cadf.model.CadfOutcomes;
import io.maestro3.cadf.model.CadfResource;
import io.maestro3.cadf.model.CadfResourceType;
import io.maestro3.cadf.model.CadfResourceTypes;
import io.maestro3.cadf.model.CadfTag;
import io.maestro3.sdk.internal.util.DateUtils;

import java.util.Date;
import java.util.List;


public interface CadfUtils {

    String STRING_CONTENT_TYPE = "string";
    String BOOLEAN_CONTENT_TYPE = "boolean";
    String ID_NAMESPACE = "agent:";

    String UNKNOWN = "UNKNOWN";

    CadfResource SYSTEM = CadfResource.builder()
            .ofType(CadfResourceTypes.system())
            .withId(ID_NAMESPACE + "SYSTEM")
            .withName("System")
            .build();

    static CadfResource createTarget(CadfResourceType type, String id) {
        return CadfResource.builder()
                .ofType(type)
                .withId(ID_NAMESPACE + id)
                .withName(id)
                .build();
    }

    static CadfAttachment createAttachment(String type, String name, Object content) {
        CadfAttachment attachment = new CadfAttachment(type, name);
        attachment.setContent(content);
        return attachment;
    }

    static CadfAuditEvent generateCadfAuditEvent(ICadfAction action,
                                                 Date date,
                                                 String actionId,
                                                 CadfResource target,
                                                 List<CadfMeasurement> measurements,
                                                 List<CadfAttachment> attachments,
                                                 List<CadfTag> tags,
                                                 CadfResource initiator) {
        return CadfAuditEvent.builder()
                .withId(CadfUtils.ID_NAMESPACE + actionId)
                .withAction(action)
                .withEventTime(DateUtils.formatDate(date, DateUtils.CADF_FORMAT_TIME))
                .withEventType(CadfEventType.ACTIVITY)
                .withInitiator(initiator)
                .withObserver(CadfUtils.SYSTEM)
                .withTarget(target)
                .withOutcome(CadfOutcomes.success())
                .withMeasurements(measurements)
                .withAttachments(attachments)
                .withTags(tags)
                .build();
    }
}
