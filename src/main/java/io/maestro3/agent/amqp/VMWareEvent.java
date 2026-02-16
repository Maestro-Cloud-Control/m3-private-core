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

import java.util.Date;
import java.util.Objects;

public class VMWareEvent {

    private String cloud;
    private String organization;
    private String type;
    private String entityId;
    private String entityType;
    private Date date;

    public VMWareEvent(String cloud, String organization, String type, String entityId, String entityType, Date date) {
        this.cloud = cloud;
        this.organization = organization;
        this.type = type;
        this.entityId = entityId;
        this.entityType = entityType;
        this.date = date;
    }

    public String getCloud() {
        return cloud;
    }

    public String getOrganization() {
        return organization;
    }

    public String getType() {
        return type;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public Date getDate() {
        return date;
    }

}
