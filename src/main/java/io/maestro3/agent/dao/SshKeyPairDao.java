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

import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.base.SshKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SshKeyPairDao implements ISshKeyPairDao {
    protected static final String COLLECTION = "SshKeyPair";
    protected MongoTemplate template;

    @Autowired
    public SshKeyPairDao(MongoTemplate template) {
        this.template = template;
    }

    @Override
    public List<SshKeyPair> findAll() {
        return template.findAll(SshKeyPair.class, COLLECTION);
    }

    @Override
    public void save(SshKeyPair keyPair) {
        template.insert(keyPair, COLLECTION);
    }

    @Override
    public void delete(PrivateCloudType cloudType, String tenant, String region, String keyName) {
        Criteria criteria = Criteria.where("cloud").is(cloudType)
            .and("tenant").is(tenant)
            .and("name").is(keyName)
            .and("region").is(region);
        template.remove(Query.query(criteria), COLLECTION);
    }

    @Override
    public SshKeyPair get(PrivateCloudType cloudType, String tenant, String region, String keyName) {
        Criteria criteria = Criteria.where("cloud").is(cloudType)
            .and("tenant").is(tenant)
            .and("name").is(keyName)
            .and("region").is(region);
        return template.findOne(Query.query(criteria), SshKeyPair.class, COLLECTION);
    }
}
