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

import org.springframework.data.annotation.Id;

import java.util.Date;

public class NativeAuditEvent {
    @Id
    private String uuid;
    private String regionId;
    private String tenantId;
    private Date receivedDate;
    private String content;
    private boolean valid;

    public NativeAuditEvent(String uuid, String regionId, Date receivedDate, String content, boolean valid) {
        this.regionId = regionId;
        this.receivedDate = receivedDate;
        this.content = content;
    }

    public NativeAuditEvent(String uuid, String regionId, String tenantId, Date receivedDate, String content, boolean valid) {
        this(uuid, regionId, receivedDate, content, valid);
        this.tenantId = tenantId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
