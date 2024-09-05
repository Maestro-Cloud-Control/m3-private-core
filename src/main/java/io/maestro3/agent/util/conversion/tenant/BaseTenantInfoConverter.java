package io.maestro3.agent.util.conversion.tenant;

import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.util.PrivateAgentStateUpdater;

import java.util.HashMap;
import java.util.Map;

public class BaseTenantInfoConverter implements TenantInfoConverter<ITenant> {

    private static final String ACTIVATION_DATE = "activationDate";
    private static final String STATUS = "status";
    private static final String TENANT_NAME = "tenantName";


    @Override
    public Map<String, Object> getTenantInfo(ITenant tenant) {
        Map<String, Object> data = new HashMap<>();
        data.put(TENANT_NAME, tenant.getTenantAlias());
        data.put(ACTIVATION_DATE, PrivateAgentStateUpdater.convertToTimestampFrom(tenant.getId()));
        data.put(STATUS, tenant.getTenantState());
        return data;
    }

    @Override
    public ITenant convert(ITenant tenant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getInfo(ITenant tenant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrivateCloudType getPrivateCloudType() {
        return null;
    }
}
