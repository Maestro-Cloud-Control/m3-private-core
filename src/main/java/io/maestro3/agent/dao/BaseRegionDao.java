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

import io.maestro3.agent.model.base.IAmqpSupportedRegion;
import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.PrivateCloudType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public abstract class BaseRegionDao<R extends IRegion> implements IRegionRepository<R> {
    protected static final String COLLECTION = "Regions";
    protected MongoTemplate template;
    protected PrivateCloudType cloudType;

    @Autowired
    public BaseRegionDao(MongoTemplate template, PrivateCloudType cloudType) {
        this.template = template;
        this.cloudType = cloudType;
    }

    @Override
    public List<R> findAllRegionsForCloud() {
        Criteria criteria = Criteria.where("cloud").is(cloudType.toString());
        return template.find(Query.query(criteria), IRegion.class, COLLECTION).stream()
                .map(r -> (R) r)
                .collect(Collectors.toList());
    }

    @Override
    public R findByIdInCloud(String id) {
        Criteria criteria = Criteria.where("_id").is(id)
                .and("cloud").is(cloudType.toString());
        return (R) template.findOne(Query.query(criteria), IRegion.class, COLLECTION);
    }

    @Override
    public R findByAliasInCloud(String alias) {
        Criteria criteria = Criteria.where("regionAlias").is(alias)
                .and("cloud").is(cloudType.toString());
        return (R) template.findOne(Query.query(criteria), IRegion.class, COLLECTION);
    }

    @Override
    public List<IAmqpSupportedRegion> findAllAmqpRegions() {
        Criteria criteria = Criteria.where("rabbitNotificationConfig").exists(true);
        return template.find(Query.query(criteria), IAmqpSupportedRegion.class, COLLECTION);
    }

    @Override
    public IRegion findByRegionAlias(String regionAlias) {
        Criteria criteria = Criteria.where("regionAlias").is(regionAlias);
        return template.findOne(Query.query(criteria), IRegion.class, COLLECTION);
    }

    @Override
    public void delete(R cloud) {
        Criteria criteria = Criteria.where("_id").is(cloud.getId());
        template.remove(Query.query(criteria), COLLECTION);
    }

    @Override
    public void save(R cloud) {
        template.save(cloud, COLLECTION);
    }

    @Override
    public IRegion findById(String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        return template.findOne(Query.query(criteria), IRegion.class, COLLECTION);
    }

    @Override
    public List<IRegion> findAll() {
        return template.findAll(IRegion.class, COLLECTION);
    }

    @Override
    public List<IRegion> findByCloud(String cloudName) {
        Criteria criteria = Criteria.where("cloud").is(cloudName);
        return template.find(Query.query(criteria), IRegion.class, COLLECTION);
    }

    @Override
    public PrivateCloudType getCloud() {
        return cloudType;
    }
}
