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

import io.maestro3.agent.chef.dao.IKeyValueDao;
import io.maestro3.agent.chef.model.KeyValueProperty;
import io.maestro3.chef.model.ChefConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Serhii Akhmetshin
 * Created: 19/12/2023
 */
@Repository
public class KeyValueDao implements IKeyValueDao {
    private MongoOperations operations;

    @Autowired
    public KeyValueDao(MongoOperations mongo) {
        this.operations = mongo;
    }

    public List<ChefConfiguration> findAll() {
        return operations.findAll(ChefConfiguration.class, KeyValueProperty.TABLE);
    }

    @Override
    public String getValue(String key) {
        Criteria criteria = Criteria.where("key").is(key);
        KeyValueProperty one = operations.findOne(Query.query(criteria), KeyValueProperty.class, KeyValueProperty.TABLE);
        return one == null ? null : one.getValue();
    }

    @Override
    public void remove(String key) {
        Criteria criteria = Criteria.where("key").is(key);
        operations.remove(Query.query(criteria), KeyValueProperty.class, KeyValueProperty.TABLE);
    }

    @Override
    public void save(String key, String value) {
        KeyValueProperty objectToSave = new KeyValueProperty();
        objectToSave.setKey(key);
        objectToSave.setValue(value);
        operations.save(objectToSave, KeyValueProperty.TABLE);
    }
}
