package io.maestro3.agent.platform.model.params;


import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;

import java.util.Map;

public class ActivatePlatformServiceParams {
    
    private String serviceName;
    private String cloud;
    private String tenantDisplayName;
    private String regionName;
    private Map<String, TerraformUserVariable> variables;
    private String requester;
    private int expireAfterHours = -1;


    public String getServiceName() {
        return serviceName;
    }

    public ActivatePlatformServiceParams withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getCloud() {
        return cloud;
    }

    public ActivatePlatformServiceParams withCloud(String cloud) {
        this.cloud = cloud;
        return this;
    }

    public String getTenantDisplayName() {
        return tenantDisplayName;
    }

    public ActivatePlatformServiceParams withTenantDisplayName(String tenantDisplayName) {
        this.tenantDisplayName = tenantDisplayName;
        return this;
    }

    public String getRegionName() {
        return regionName;
    }

    public ActivatePlatformServiceParams withRegionName(String regionName) {
        this.regionName = regionName;
        return this;
    }

    public Map<String, TerraformUserVariable> getVariables() {
        return variables;
    }

    public ActivatePlatformServiceParams withVariables(Map<String, TerraformUserVariable> variables) {
        this.variables = variables;
        return this;
    }

    public String getRequester() {
        return requester;
    }

    public ActivatePlatformServiceParams withRequester(String requester) {
        this.requester = requester;
        return this;
    }

    public int getExpireAfterHours() {
        return expireAfterHours;
    }

    public ActivatePlatformServiceParams withExpireAfterHours(int expireAfterHours) {
        this.expireAfterHours = expireAfterHours;
        return this;
    }
}
