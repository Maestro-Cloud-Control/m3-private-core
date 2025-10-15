package io.maestro3.agent.platform.model.params;

import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;

public class ListPlatformServiceDefinitionsParams {

    private ITenant tenant;
    private String serviceName;
    private PlatformServiceDeploymentType deploymentType;

    public ITenant getTenant() {
        return tenant;
    }

    public ListPlatformServiceDefinitionsParams withTenant(ITenant tenant) {
        this.tenant = tenant;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ListPlatformServiceDefinitionsParams withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public PlatformServiceDeploymentType getDeploymentType() {
        return deploymentType;
    }

    public ListPlatformServiceDefinitionsParams withDeploymentType(PlatformServiceDeploymentType deploymentType) {
        this.deploymentType = deploymentType;
        return this;
    }
}
