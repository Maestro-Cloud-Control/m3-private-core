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

package io.maestro3.agent.chef.dao.impl;

import io.maestro3.chef.dao.IChefConfigurationDao;
import io.maestro3.chef.model.ChefConfiguration;
import io.maestro3.sdk.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Serhii Akhmetshin
 * Created: 19/12/2023
 */
@Repository
public class ChefConfigurationDao implements IChefConfigurationDao {
    private MongoOperations operations;

    @Autowired
    public ChefConfigurationDao(MongoOperations mongo) {
        this.operations = mongo;
    }

    @Override
    public ChefConfiguration findByRegionName(String region) {
        Assert.hasText(region, "region can't be null or empty.");

        return findAll().stream()
                .filter(config -> config.getZones().contains(region))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ChefConfiguration find(String serverId) {
        Assert.hasText(serverId, "serverId can't be null or empty.");

        return findAll().stream()
                .filter(config -> config.getServerId().equals(serverId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(ChefConfiguration chefConfiguration) {
        Assert.notNull(chefConfiguration, "chef configuration can't be null or empty.");

        operations.save(chefConfiguration);

    }

    @Override
    public void update(ChefConfiguration chefConfiguration) {
        Assert.notNull(chefConfiguration, "chefConfiguration can't be null.");

        operations.save(chefConfiguration);
    }

    @Override
    public ChefConfiguration findByTenantInRegionId(String tenantInRegionId) {
        Assert.hasText(tenantInRegionId, "tenantInRegionId can't be null or empty.");

        return findAll().stream()
                .filter(config -> config.getTenantInRegionId().equals(tenantInRegionId))
                .findFirst()
                .orElse(null);
    }

    public List<ChefConfiguration> findAll() {
        return operations.findAll(ChefConfiguration.class, ChefConfiguration.TABLE_NAME);
    }
}
