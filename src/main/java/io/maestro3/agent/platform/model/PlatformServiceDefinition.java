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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Document(collection = "PlatformServiceDefinitions")
public class PlatformServiceDefinition{

    @Id
    private String serviceDefinitionId;
    @Field(Fields.NAME)
    @JsonProperty(Fields.NAME)
    private String name;
    @Field(Fields.TITLE)
    @JsonProperty(Fields.TITLE)
    private String title;
    @Field(Fields.SUPPORTED_CLOUDS)
    @JsonProperty(Fields.SUPPORTED_CLOUDS)
    private Set<String> supportedClouds;
    @Field(Fields.TENANT_DISPLAY_NAME)
    @JsonProperty(Fields.TENANT_DISPLAY_NAME)
    private String tenantDisplayName;
    @Field(Fields.DESCRIPTION)
    @JsonProperty(Fields.DESCRIPTION)
    private String description;
    @Field(Fields.DISCOVERABLE_URL)
    @JsonProperty(Fields.DISCOVERABLE_URL)
    private String discoverableUrl;
    @Field(Fields.SERVICE_DEPLOYMENT_INFO)
    @JsonProperty(Fields.SERVICE_DEPLOYMENT_INFO)
    private PlatformServiceDeploymentInfo serviceDeploymentInfo;
    @Field(Fields.PRODUCT_VERSION)
    @JsonProperty(Fields.PRODUCT_VERSION)
    private String productVersion;
    @Field(Fields.CATEGORIES)
    @JsonProperty(Fields.CATEGORIES)
    private Set<String> categories;
    @Field(Fields.OPERATING_SYSTEM)
    @JsonProperty(Fields.OPERATING_SYSTEM)
    private String operatingSystem;
    @Field(Fields.DELIVERY_METHOD)
    @JsonProperty(Fields.DELIVERY_METHOD)
    private String deliveryMethod;
    @Field(Fields.USAGE)
    @JsonProperty(Fields.USAGE)
    private Map<String, String> usage;
    @Field(Fields.SUPPORT)
    @JsonProperty(Fields.SUPPORT)
    private Map<String, String> support;

    public PlatformServiceDefinition() {
        this.serviceDefinitionId = UUID.randomUUID().toString();
    }

    public String getServiceDefinitionId() {
        return serviceDefinitionId;
    }

    public String getName() {
        return name;
    }

    public PlatformServiceDefinition withName(String name) {
        this.name = name;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PlatformServiceDefinition withTitle(String title) {
        this.title = title;
        return this;
    }

    public Set<String> getSupportedClouds() {
        return supportedClouds;
    }

    public PlatformServiceDefinition withSupportedClouds(Set<String> supportedClouds) {
        this.supportedClouds = supportedClouds;
        return this;
    }

    public String getTenantDisplayName() {
        return tenantDisplayName;
    }

    public PlatformServiceDefinition withTenantDisplayName(String tenantDisplayName) {
        this.tenantDisplayName = tenantDisplayName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PlatformServiceDefinition withDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDiscoverableUrl() {
        return discoverableUrl;
    }

    public PlatformServiceDefinition withDiscoverableUrl(String discoverableUrl) {
        this.discoverableUrl = discoverableUrl;
        return this;
    }

    public PlatformServiceDeploymentInfo getServiceDeploymentInfo() {
        return serviceDeploymentInfo;
    }

    public PlatformServiceDefinition withServiceDeploymentInfo(PlatformServiceDeploymentInfo serviceDeploymentInfo) {
        this.serviceDeploymentInfo = serviceDeploymentInfo;
        return this;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public PlatformServiceDefinition withProductVersion(String productVersion) {
        this.productVersion = productVersion;
        return this;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public PlatformServiceDefinition withCategories(Set<String> categories) {
        this.categories = categories;
        return this;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public PlatformServiceDefinition withOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
        return this;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public PlatformServiceDefinition withDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
        return this;
    }

    public Map<String, String> getUsage() {
        return usage;
    }

    public PlatformServiceDefinition withUsage(Map<String, String> usage) {
        this.usage = usage;
        return this;
    }

    public Map<String, String> getSupport() {
        return support;
    }

    public PlatformServiceDefinition withSupport(Map<String, String> support) {
        this.support = support;
        return this;
    }

    public static final class Fields {
        public static final String SERVICE_DEFINITION_ID = "serviceDefinitionId";
        public static final String NAME = "name";
        public static final String TITLE = "title";
        public static final String SUPPORTED_CLOUDS = "supportedClouds";
        public static final String TENANT_DISPLAY_NAME = "tenantDisplayName";
        public static final String DESCRIPTION = "description";
        public static final String DISCOVERABLE_URL = "discoverableUrl";
        public static final String SERVICE_DEPLOYMENT_INFO = "serviceDeploymentInfo";
        public static final String PRODUCT_VERSION = "productVersion";
        public static final String CATEGORIES = "categories";
        public static final String OPERATING_SYSTEM = "operatingSystem";
        public static final String DELIVERY_METHOD = "deliveryMethod";
        public static final String USAGE = "usage";
        public static final String SUPPORT = "support";
        
        private Fields() {
            throw new UnsupportedOperationException("Class is not designed for an instantiation");
        }
    }
}
