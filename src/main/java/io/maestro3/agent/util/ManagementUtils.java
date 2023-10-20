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

package io.maestro3.agent.util;

import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.base.BaseRegion;
import io.maestro3.agent.model.base.BaseTenant;
import io.maestro3.agent.model.base.TenantState;


public final class ManagementUtils {

    private ManagementUtils() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }

    public static void isManagementAvailable(BaseRegion cloud, BaseTenant organization) {
        if (!cloud.isManagementAvailable() || !organization.isManagementAvailable()) {
            throw new ReadableAgentException(String.format("Management operation is not supported for %s in %s", organization.getTenantAlias(), cloud.getRegionAlias()));
        }
        if (!organization.isSkipHealthCheck() && !TenantState.AVAILABLE.equals(organization.getTenantState())) {
            throw new ReadableAgentException(String.format("Tenant %s in region %s is currently unavailable or under configuration check." +
                " Try again later or contact support team", organization.getTenantAlias(), cloud.getRegionAlias()));
        }
    }
}
