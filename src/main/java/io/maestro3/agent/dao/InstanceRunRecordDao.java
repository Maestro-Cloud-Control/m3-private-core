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

import io.maestro3.agent.model.base.InstanceRunRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class InstanceRunRecordDao implements IInstanceRunRecordDao {

    protected static final String COLLECTION = "InstanceRunRecord";
    protected MongoTemplate template;

    @Autowired
    public InstanceRunRecordDao(MongoTemplate template) {
        this.template = template;
    }

    @Override
    public List<InstanceRunRecord> findAll() {
        return template.findAll(InstanceRunRecord.class, COLLECTION);
    }

    @Override
    public void save(InstanceRunRecord lock) {
        template.insert(lock, COLLECTION);
    }

    @Override
    public void deleteAll() {
        template.remove(Query.query(Criteria.where("_id").exists(true)), COLLECTION);
    }
}
