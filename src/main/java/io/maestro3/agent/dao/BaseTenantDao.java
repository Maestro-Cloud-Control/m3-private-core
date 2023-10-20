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

package io.maestro3.agent.dao;

import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.model.base.PrivateCloudType;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseTenantDao<TENANT extends ITenant> implements ITenantRepository<TENANT> {
    protected static final String COLLECTION = "Tenants";
    protected MongoTemplate template;
    protected PrivateCloudType cloudType;

    public BaseTenantDao(MongoTemplate template, PrivateCloudType cloudType) {
        this.template = template;
        this.cloudType = cloudType;
    }

    @Override
    public ITenant findByTenantAliasAndRegionId(String tenantAlias, String regionId) {
        Criteria criteria = Criteria.where("tenantAlias").is(tenantAlias)
            .and("regionId").is(regionId);
        return template.findOne(Query.query(criteria), ITenant.class, COLLECTION);
    }

    @Override
    public List<ITenant> findAll() {
        return template.findAll(ITenant.class, COLLECTION);
    }

    @Override
    public List<ITenant> findForCloud(String cloudType) {
        Criteria criteria = Criteria.where("cloud").is(cloudType);
        return template.find(Query.query(criteria), ITenant.class, COLLECTION);
    }

    @Override
    public List<ITenant> findByRegionId(String regionId) {
        Criteria criteria = Criteria.where("regionId").is(regionId);
        return template.find(Query.query(criteria), ITenant.class, COLLECTION);
    }

    @Override
    public void save(ITenant tenant) {
        template.save(tenant, COLLECTION);
    }

    @Override
    public void delete(TENANT tenant) {
        template.remove(tenant, COLLECTION);
    }

    @Override
    public List<TENANT> findByRegionIdInCloud(String regionId) {
        Criteria criteria = Criteria.where("regionId").is(regionId)
            .and("cloud").is(cloudType.toString());
        List<ITenant> tenants = template.find(Query.query(criteria), ITenant.class, COLLECTION);
        return tenants.stream()
            .map(t -> (TENANT) t)
            .collect(Collectors.toList());
    }

    @Override
    public List<TENANT> findAllInCloud() {
        Criteria criteria = Criteria.where("cloud").is(cloudType.toString());
        List<ITenant> tenants = template.find(Query.query(criteria), ITenant.class, COLLECTION);
        return tenants.stream()
            .map(t -> (TENANT) t)
            .collect(Collectors.toList());
    }

    @Override
    public TENANT findByTenantAliasAndRegionIdInCloud(String tenantAlias, String regionId) {

        Criteria criteria = Criteria.where("tenantAlias").is(tenantAlias)
            .and("regionId").is(regionId)
            .and("cloud").is(cloudType.toString());
        ITenant one = template.findOne(Query.query(criteria), ITenant.class, COLLECTION);
        return (TENANT) one;
    }

    @Override
    public PrivateCloudType getCloud() {
        return cloudType;
    }
}
