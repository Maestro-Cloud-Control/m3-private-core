package io.maestro3.agent.util.conversion.tenant;

import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.model.base.PrivateCloudType;

import java.util.Map;

public interface TenantInfoConverter<T extends ITenant> {

    default Map<String, Object> getTenantInfo(ITenant tenant) {
        T convertedTenant = convert(tenant);
        return getInfo(convertedTenant);
    }

    T convert(ITenant tenant);

    Map<String, Object> getInfo(T tenant);

    PrivateCloudType getPrivateCloudType();
}
