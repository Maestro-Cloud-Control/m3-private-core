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

import javax.validation.constraints.NotBlank;


public abstract class BaseTenant implements ITenant {

    @Id
    private String id;
    @NotBlank
    private String tenantAlias;
    @NotBlank
    private String regionId;
    private PrivateCloudType cloud;
    private TenantState tenantState = TenantState.UNKNOWN;
    private long lastStatusUpdate;
    private boolean managementAvailable = true;
    private boolean skipHealthCheck = false;
    private boolean describeAllInstances;

    public BaseTenant() {
    }

    @Override
    public boolean isDescribeAllInstances() {
        return describeAllInstances;
    }

    @Override
    public void setDescribeAllInstances(boolean describeAllInstances) {
        this.describeAllInstances = describeAllInstances;
    }

    public BaseTenant(PrivateCloudType cloud) {
        this.cloud = cloud;
    }

    @Override
    public PrivateCloudType getCloud() {
        return cloud;
    }

    public void setCloud(PrivateCloudType cloud) {
        this.cloud = cloud;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TenantState getTenantState() {
        return tenantState;
    }

    public void setTenantState(TenantState tenantState) {
        this.tenantState = tenantState;
    }

    @Override
    public long getLastStatusUpdate() {
        return lastStatusUpdate;
    }

    @Override
    public void setLastStatusUpdate(long lastStatusUpdate) {
        this.lastStatusUpdate = lastStatusUpdate;
    }

    @Override
    public boolean isManagementAvailable() {
        return managementAvailable;
    }

    @Override
    public void setManagementAvailable(boolean managementAvailable) {
        this.managementAvailable = managementAvailable;
    }

    public String getId() {
        return id;
    }

    public String getTenantAlias() {
        return tenantAlias;
    }

    public void setTenantAlias(String tenantAlias) {
        this.tenantAlias = tenantAlias;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    @Override
    public boolean isSkipHealthCheck() {
        return skipHealthCheck;
    }

    @Override
    public void setSkipHealthCheck(boolean skipHealthCheck) {
        this.skipHealthCheck = skipHealthCheck;
    }

    @Override
    public String toString() {
        return "BaseTenant{" +
                "id='" + id + '\'' +
                ", tenantAlias='" + tenantAlias + '\'' +
                ", regionId='" + regionId + '\'' +
                '}';
    }
}
