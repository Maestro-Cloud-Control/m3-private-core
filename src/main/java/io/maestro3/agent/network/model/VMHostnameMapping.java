/*
 * Copyright 2026 Maestro Cloud Control LLC
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

package io.maestro3.agent.network.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

/**
 * Persistent mapping between VM and custom hostname.
 */
@Document(collection = "VMHostnameMapping")
@CompoundIndex(unique = true, def = "{'hostname': 1, 'tenantId': 1, 'cloudId': 1}")
public class VMHostnameMapping {

    @Id
    private String id;

    @Indexed
    private String vmId;

    private String hostname;
    private String fullDnsName;
    private String tenantId;
    private String cloudId;
    private String dnsZone;
    private HostnameStatus status;
    private long createdDate;
    private long activatedDate;
    private long updatedDate;
    private long expiresAt;
    private String currentIpAddress;
    private String requestedBy;
    private String notes;

    public VMHostnameMapping() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getFullDnsName() {
        return fullDnsName;
    }

    public void setFullDnsName(String fullDnsName) {
        this.fullDnsName = fullDnsName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public String getDnsZone() {
        return dnsZone;
    }

    public void setDnsZone(String dnsZone) {
        this.dnsZone = dnsZone;
    }

    public HostnameStatus getStatus() {
        return status;
    }

    public void setStatus(HostnameStatus status) {
        this.status = status;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getActivatedDate() {
        return activatedDate;
    }

    public void setActivatedDate(long activatedDate) {
        this.activatedDate = activatedDate;
    }

    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getCurrentIpAddress() {
        return currentIpAddress;
    }

    public void setCurrentIpAddress(String currentIpAddress) {
        this.currentIpAddress = currentIpAddress;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VMHostnameMapping that = (VMHostnameMapping) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "VMHostnameMapping{" +
                "id='" + id + '\'' +
                ", vmId='" + vmId + '\'' +
                ", hostname='" + hostname + '\'' +
                ", fullDnsName='" + fullDnsName + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", cloudId='" + cloudId + '\'' +
                ", status=" + status +
                '}';
    }
}

