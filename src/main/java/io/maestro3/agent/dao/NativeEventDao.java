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

import io.maestro3.agent.model.base.NativeAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;


@Service
public class NativeEventDao implements INativeEventDao {
    private static final Logger LOG = LoggerFactory.getLogger(NativeEventDao.class);

    protected static final String COLLECTION = "NativeEvents";
    protected MongoTemplate template;

    @Autowired
    public NativeEventDao(MongoTemplate template) {
        this.template = template;
    }


    @Override
    public void save(NativeAuditEvent event) {
        try {
            template.insert(event, COLLECTION);
        } catch (Exception ex) {
            LOG.error("Failed to save event", ex);
        }
    }
}
