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

import io.maestro3.chef.dao.IChefInstanceDao;
import io.maestro3.chef.model.AutoConfigurationState;
import io.maestro3.chef.model.ChefInstance;
import io.maestro3.chef.model.InstanceProperty;
import io.maestro3.sdk.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author Serhii Akhmetshin
 * Created: 19/12/2023
 */
@Repository
public class ChefInstanceDao implements IChefInstanceDao {
    private static final String TABLE = "ChefInstances";
    private final MongoOperations mongo;

    @Autowired
    public ChefInstanceDao(MongoOperations mongo) {
        this.mongo = mongo;
    }

    @Override
    public ChefInstance find(String instanceId) {
        Query query = Query.query(Criteria.where("instanceId").is(instanceId));
        return mongo.findOne(query, ChefInstance.class, TABLE);
    }

    @Override
    public List<ChefInstance> findAllInRegion(String tenantName, String regionId) {
        Query query = Query.query(Criteria.where("tenantName").is(tenantName)
                .and("regionId").is(regionId));
        return mongo.find(query, ChefInstance.class, TABLE);
    }

    @Override
    public ChefInstance findByResourceId(String resourceId) {
        Query query = Query.query(Criteria.where("resourceId").is(resourceId));
        return mongo.findOne(query, ChefInstance.class, TABLE);
    }

    @Override
    public ChefInstance findByTenantInRegionAndInstanceId(String tenantName, String regionId, String instanceId) {
        Query query = Query.query(Criteria.where("tenantName").is(tenantName)
                .and("regionId").is(regionId)
                .and("resourceId").regex(instanceId));
        return mongo.findOne(query, ChefInstance.class, TABLE);
    }

    @Override
    public void removeByResourceId(String resourceId) {
        Query query = Query.query(Criteria.where("resourceId").is(resourceId));
        mongo.remove(query, TABLE);
    }

    @Override
    public ChefInstance findByIp(String ip) {
        Assert.hasText(ip, "IP can't be null or empty.");
        Criteria criteria = new Criteria().orOperator(
                where("privateIp").is(ip),
                where("publicIp").is(ip)
        ).and("deleted").ne(true);
        return mongo.findOne(query(criteria), ChefInstance.class, TABLE);
    }

    @Override
    public void setAutoConfigurationState(ChefInstance instance, AutoConfigurationState state) {
        Update update = new Update();
        update.set("autoConfigurationState", state);

        mongo.findAndModify(query(where("instanceId").is(instance.getInstanceId())), update,
                ChefInstance.class, TABLE);

        instance.setAutoConfigurationState(state);
    }

    @Override
    public void updateInstanceNetworkSettings(String resourceId, String publicIp, String privateIp, String publicDns) {
        Update update = new Update();
        update.set("publicIp", publicIp);
        update.set("privateIp", privateIp);
        update.set("fqdn", publicDns);

        mongo.findAndModify(query(where("resourceId").is(resourceId)), update,
                ChefInstance.class, TABLE);
    }

    @Override
    public void setChefServer(ChefInstance instance, String chefServer, String chefMode) {
        Update update = new Update();
        update.set("chefServer", chefServer);
        update.set("chefMode", chefMode);

        mongo.findAndModify(query(where("instanceId").is(instance.getInstanceId())), update,
                ChefInstance.class, TABLE);

        instance.setChefServer(chefServer);
        instance.setChefMode(chefMode);
    }

    @Override
    public void setRoles(ChefInstance instance, Set<String> roles) {
        Update update = new Update();
        update.set("roles", roles);

        mongo.findAndModify(query(where("instanceId").is(instance.getInstanceId())), update,
                ChefInstance.class, TABLE);

        instance.setRoles(roles);
    }

    @Override
    public void setConfigurationReceived(ChefInstance instance) {
        Update update = new Update();
        update.set("configurationReceived", true);
        update.inc("configurationReceiveAttempts", 1);

        mongo.findAndModify(query(where("instanceId").is(instance.getInstanceId())), update,
                ChefInstance.class, TABLE);

        instance.setConfigurationReceived(true);
        instance.setConfigurationReceiveAttempts(instance.getConfigurationReceiveAttempts() + 1);
    }

    @Override
    public void setSshKeySet(ChefInstance instance) {
        Update update = new Update();
        update.set("sshKeySet", true);

        mongo.findAndModify(query(where("instanceId").is(instance.getInstanceId())), update,
                ChefInstance.class, TABLE);

        instance.setSshKeySet(true);
    }

    @Override
    public String getInstancePublicKey(String instanceId) {
        Assert.hasText(instanceId, "instanceId can't be null or empty.");

        return null;
    }

    @Override
    public void setProperties(ChefInstance instance, List<InstanceProperty> instanceProperties) {
        Update update = new Update();
        update.set("instanceProperties", instanceProperties);

        mongo.findAndModify(query(where("instanceId").is(instance.getInstanceId())), update,
                ChefInstance.class, TABLE);

        instance.setInstanceProperties(instanceProperties);
    }

    @Override
    public void setProperties(String instanceId, List<InstanceProperty> instanceProperties) {
        Update update = new Update();
        update.set("instanceProperties", instanceProperties);

        mongo.findAndModify(query(where("instanceId").is(instanceId)), update,
                ChefInstance.class, TABLE);
    }

    @Override
    public void saveInstance(ChefInstance chefInstance) {
        mongo.insert(chefInstance);
    }

    @Override
    public void setScriptFiles(ChefInstance instance, Map<String, String> scriptFiles) {
        Update update = new Update();
        update.set("scriptFiles", scriptFiles);

        mongo.findAndModify(query(where("instanceId").is(instance.getInstanceId())), update,
                ChefInstance.class, TABLE);

        instance.setScriptFiles(scriptFiles);

    }

    private class DbEntitiesAnnotationsScanner {
    }
}
