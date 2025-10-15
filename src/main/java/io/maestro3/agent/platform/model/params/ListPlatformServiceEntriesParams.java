package io.maestro3.agent.platform.model.params;

import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;

public class ListPlatformServiceEntriesParams {

    private IRegion region;
    private ITenant tenant;
    private String owner;
    private String serviceEntryId;
    private String serviceName;

    public IRegion getRegion() {
        return region;
    }

    public ListPlatformServiceEntriesParams withRegion(IRegion region) {
        this.region = region;
        return this;
    }

    public ITenant getTenant() {
        return tenant;
    }

    public ListPlatformServiceEntriesParams withTenant(ITenant tenant) {
        this.tenant = tenant;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public ListPlatformServiceEntriesParams withOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getServiceEntryId() {
        return serviceEntryId;
    }

    public ListPlatformServiceEntriesParams withServiceEntryId(String serviceEntryId) {
        this.serviceEntryId = serviceEntryId;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ListPlatformServiceEntriesParams withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }
}
