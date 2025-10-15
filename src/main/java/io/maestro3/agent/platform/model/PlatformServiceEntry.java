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

package io.maestro3.agent.platform.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Document(collection = "PlatformServiceEntries")
public class PlatformServiceEntry {

    @Id
    private String serviceEntryId = UUID.randomUUID().toString();
    @Field(Fields.SERVICE_NAME)
    @JsonProperty(Fields.SERVICE_NAME)
    private String serviceName;
    @Field(Fields.DEPLOYMENT_INFO)
    @JsonProperty(Fields.DEPLOYMENT_INFO)
    private PlatformServiceDeploymentInfo deploymentInfo;
    @Field(Fields.CLOUD)
    @JsonProperty(Fields.CLOUD)
    private String cloud;
    @Field(Fields.TENANT_NAME)
    @JsonProperty(Fields.TENANT_NAME)
    private String tenantName;
    @Field(Fields.TENANT_DISPLAY_NAME)
    @JsonProperty(Fields.TENANT_DISPLAY_NAME)
    private String tenantDisplayName;
    @Field(Fields.REGION_NAME)
    @JsonProperty(Fields.REGION_NAME)
    private String regionName;
    @Field(Fields.SERVICE_URL)
    @JsonProperty(Fields.SERVICE_URL)
    private String serviceUrl;
    @Field(Fields.OWNER)
    @JsonProperty(Fields.OWNER)
    private String owner;
    @Field(Fields.STATE)
    @JsonProperty(Fields.STATE)
    private PlatformServiceEntryState state;

    public PlatformServiceEntry() {
    }



    public String getServiceEntryId() {
        return serviceEntryId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public PlatformServiceEntry withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public PlatformServiceEntry withServiceEntryId(String serviceEntryId) {
        this.serviceEntryId = serviceEntryId;
        return this;
    }

    public PlatformServiceDeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public PlatformServiceEntry withDeploymentInfo(PlatformServiceDeploymentInfo deploymentInfo) {
        this.deploymentInfo = deploymentInfo;
        return this;
    }

    public String getCloud() {
        return cloud;
    }

    public PlatformServiceEntry withCloud(String cloud) {
        this.cloud = cloud;
        return this;
    }

    public String getTenantName() {
        return tenantName;
    }

    public PlatformServiceEntry withTenantName(String tenantName) {
        this.tenantName = tenantName;
        return this;
    }

    public String getTenantDisplayName() {
        return tenantDisplayName;
    }

    public PlatformServiceEntry withTenantDisplayName(String tenantDisplayName) {
        this.tenantDisplayName = tenantDisplayName;
        return this;
    }

    public String getRegionName() {
        return regionName;
    }

    public PlatformServiceEntry withRegionName(String regionName) {
        this.regionName = regionName;
        return this;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public PlatformServiceEntry withServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public PlatformServiceEntry withOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public PlatformServiceEntryState getState() {
        return state;
    }

    public PlatformServiceEntry withState(PlatformServiceEntryState state) {
        this.state = state;
        return this;
    }

    public static final class Fields {
        public static final String SERVICE_ENTRY_ID = "serviceEntryId";
        public static final String SERVICE_NAME = "serviceName";
        public static final String DEPLOYMENT_INFO = "deploymentInfo";
        public static final String CLOUD = "cloud";
        public static final String TENANT_NAME = "tenantName";
        public static final String TENANT_DISPLAY_NAME = "tenantDisplayName";
        public static final String REGION_NAME = "regionName";
        public static final String SERVICE_URL = "serviceUrl";
        public static final String OWNER = "owner";
        public static final String STATE = "state";

        private Fields() {
            throw new UnsupportedOperationException("Class is not designed for an instantiation");
        }
    }
}
