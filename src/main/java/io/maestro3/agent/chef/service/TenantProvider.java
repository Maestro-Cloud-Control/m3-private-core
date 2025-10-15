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

package io.maestro3.agent.chef.service;

import io.maestro3.agent.dao.IRegionRepository;
import io.maestro3.agent.dao.ITenantRepository;
import io.maestro3.agent.model.base.BaseTenant;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.chef.service.ITenantProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Serhii Akhmetshin
 * Created: 04/09/2024
 */
@Service
public class TenantProvider implements ITenantProvider {
    private ITenantRepository tenantRepository;

    @Autowired
    public TenantProvider(ITenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public String getTenantAlias(String name) {
        String[] split = name.split(BaseTenant.NAME_SPLITERATOR);
        String regionId = split[0];
        String tenantAlias = split[1];
        ITenant tenant = tenantRepository.findByTenantAliasAndRegionId(tenantAlias, regionId);
        return tenant.getTenantAlias();
    }


}
