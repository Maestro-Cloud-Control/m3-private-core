package io.maestro3.agent.platform.model.params;

import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;

public class DeactivatePlatformServiceParams {

    private IRegion region;
    private ITenant tenant;
    private String serviceEntryId;
    private String requester;

    public IRegion getRegion() {
        return region;
    }

    public DeactivatePlatformServiceParams withRegion(IRegion region) {
        this.region = region;
        return this;
    }

    public ITenant getTenant() {
        return tenant;
    }

    public DeactivatePlatformServiceParams withTenant(ITenant tenant) {
        this.tenant = tenant;
        return this;
    }

    public String getServiceEntryId() {
        return serviceEntryId;
    }

    public DeactivatePlatformServiceParams withServiceEntryId(String serviceEntryId) {
        this.serviceEntryId = serviceEntryId;
        return this;
    }

    public String getRequester() {
        return requester;
    }

    public DeactivatePlatformServiceParams withRequester(String requester) {
        this.requester = requester;
        return this;
    }
}
