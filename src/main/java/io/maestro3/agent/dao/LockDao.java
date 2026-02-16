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

import io.maestro3.agent.model.base.Lock;
import io.maestro3.agent.model.base.PrivateCloudType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LockDao implements ILockDao {
    private static final Logger LOG = LoggerFactory.getLogger(LockDao.class);

    protected static final String COLLECTION = "ScheduleLocks";
    protected MongoTemplate template;

    @Autowired
    public LockDao(MongoTemplate template) {
        this.template = template;
    }

    @Override
    public List<Lock> findAll() {
        return template.findAll(Lock.class, COLLECTION);
    }

    @Override
    public boolean save(Lock lock) {
        try {
            template.insert(lock, COLLECTION);
            return true;
        } catch (Exception ex) {
            LOG.debug("Lock error", ex);
            return false;
        }
    }

    @Override
    public void delete(PrivateCloudType cloudType, String lockName) {
        template.remove(new Lock(cloudType.name(), lockName), COLLECTION);
    }
}
