package io.maestro3.agent.platform.model.params;

import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;

import java.util.Map;
import java.util.Set;

public class RegisterPlatformServiceParams {

    private String name;
    private String title;
    private String description;
    private String discoverableUrl;
    private PlatformServiceDeploymentType deploymentType;
    private Map<String, String> deploymentParams;
    private String productVersion;
    private Set<String> categories;
    private String operatingSystem;
    private String deliveryMethod;
    private Set<String> supportedClouds;
    private String tenantDisplayName;
    private boolean allTenants;
    private Map<String, String> usage;
    private Map<String, String> support;

    public String getName() {
        return name;
    }

    public RegisterPlatformServiceParams withName(String name) {
        this.name = name;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public RegisterPlatformServiceParams withTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RegisterPlatformServiceParams withDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDiscoverableUrl() {
        return discoverableUrl;
    }

    public RegisterPlatformServiceParams withDiscoverableUrl(String discoverableUrl) {
        this.discoverableUrl = discoverableUrl;
        return this;
    }

    public PlatformServiceDeploymentType getDeploymentType() {
        return deploymentType;
    }

    public RegisterPlatformServiceParams withDeploymentType(PlatformServiceDeploymentType deploymentType) {
        this.deploymentType = deploymentType;
        return this;
    }

    public Map<String, String> getDeploymentParams() {
        return deploymentParams;
    }

    public RegisterPlatformServiceParams withDeploymentParams(Map<String, String> deploymentParams) {
        this.deploymentParams = deploymentParams;
        return this;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public RegisterPlatformServiceParams withProductVersion(String productVersion) {
        this.productVersion = productVersion;
        return this;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public RegisterPlatformServiceParams withCategories(Set<String> categories) {
        this.categories = categories;
        return this;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public RegisterPlatformServiceParams withOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
        return this;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public RegisterPlatformServiceParams withDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
        return this;
    }

    public Set<String> getSupportedClouds() {
        return supportedClouds;
    }

    public RegisterPlatformServiceParams withSupportedClouds(Set<String> supportedClouds) {
        this.supportedClouds = supportedClouds;
        return this;
    }

    public String getTenantDisplayName() {
        return tenantDisplayName;
    }

    public RegisterPlatformServiceParams withTenantDisplayName(String tenantDisplayName) {
        this.tenantDisplayName = tenantDisplayName;
        return this;
    }

    public boolean isAllTenants() {
        return allTenants;
    }

    public RegisterPlatformServiceParams withAllTenants(boolean allTenants) {
        this.allTenants = allTenants;
        return this;
    }

    public Map<String, String> getUsage() {
        return usage;
    }

    public RegisterPlatformServiceParams withUsage(Map<String, String> usage) {
        this.usage = usage;
        return this;
    }

    public Map<String, String> getSupport() {
        return support;
    }

    public RegisterPlatformServiceParams withSupport(Map<String, String> support) {
        this.support = support;
        return this;
    }
}
