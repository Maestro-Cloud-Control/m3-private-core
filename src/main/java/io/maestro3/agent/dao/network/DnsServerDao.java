/*
 * Copyright 2026 Maestro Cloud Control LLC
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
package io.maestro3.agent.dao.network;

import io.maestro3.agent.network.model.DnsServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DnsServerDao implements IDnsServerDao {

    protected static final String COLLECTION = "DnsServer";
    protected MongoTemplate template;

    @Autowired
    public DnsServerDao(MongoTemplate template) {
        this.template = template;
    }

    @Override
    public List<DnsServer> findAll() {
        return template.findAll(DnsServer.class, COLLECTION);
    }

    @Override
    public DnsServer findById(String id) {
        return template.findById(id, DnsServer.class, COLLECTION);
    }

    @Override
    public DnsServer findByName(String name) {
        return template.findOne(Query.query(Criteria.where("name").is(name)), DnsServer.class, COLLECTION);
    }

    @Override
    public void save(DnsServer server) {
        template.save(server, COLLECTION);
    }
}
