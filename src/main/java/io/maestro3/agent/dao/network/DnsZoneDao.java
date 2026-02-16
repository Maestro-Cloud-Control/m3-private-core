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

import io.maestro3.agent.network.model.DnsZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DnsZoneDao implements IDnsZoneDao {

    protected static final String COLLECTION = "DnsZone";
    protected MongoTemplate template;

    @Autowired
    public DnsZoneDao(MongoTemplate template) {
        this.template = template;
    }

    @Override
    public List<DnsZone> findAll() {
        return template.findAll(DnsZone.class, COLLECTION);
    }

    @Override
    public DnsZone findById(String id) {
        return template.findById(id, DnsZone.class, COLLECTION);
    }

    @Override
    public DnsZone findByZoneFqdn(String zoneFqdn) {
        return template.findOne(Query.query(Criteria.where("zoneFqdn").is(zoneFqdn)), DnsZone.class, COLLECTION);
    }

    @Override
    public void save(DnsZone zone) {
        template.save(zone, COLLECTION);
    }
}
