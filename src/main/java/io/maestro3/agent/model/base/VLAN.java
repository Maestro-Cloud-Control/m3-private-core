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

package io.maestro3.agent.model.base;

import org.springframework.data.mongodb.core.mapping.Document;

@Document()
public abstract class VLAN {

    private String id;
    private String name;
    private String operationalSearchId;
    private String description;
    private String tenantId;
    private String regionId;
    private boolean isDmz;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperationalSearchId() {
        return operationalSearchId;
    }

    public void setOperationalSearchId(String operationalSearchId) {
        this.operationalSearchId = operationalSearchId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public boolean isDmz() {
        return isDmz;
    }

    public void setDmz(boolean dmz) {
        isDmz = dmz;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "VLAN{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", operationalSearchId='" + operationalSearchId + '\'' +
            ", description='" + description + '\'' +
            ", tenantId='" + tenantId + '\'' +
            ", regionId='" + regionId + '\'' +
            ", isDmz=" + isDmz +
            '}';
    }
}
