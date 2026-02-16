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
package io.maestro3.agent.network.service;

import io.maestro3.agent.network.client.DnsClient;
import io.maestro3.agent.network.client.DnsClientResolver;
import io.maestro3.agent.network.domain.DnsRecord;
import io.maestro3.agent.network.domain.DnsRecordType;
import io.maestro3.agent.network.domain.DnsRrsetKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DnsRecordService implements IDnsRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DnsRecordService.class);

    private final DnsClientResolver resolver;

    @Autowired
    public DnsRecordService(DnsClientResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public List<DnsRecord> listZoneRecords(String zoneFqdn) {
        String normalizedZone = DnsName.normalizeZone(zoneFqdn);
        LOGGER.debug("Listing DNS records for zone. zoneFqdn='{}', normalizedZone='{}'", zoneFqdn, normalizedZone);

        DnsClient client = resolver.resolveByZone(zoneFqdn);
        LOGGER.debug("Resolved DNS client '{}' for zone '{}'", client.getClass().getSimpleName(), normalizedZone);

        List<DnsRecord> records = client.listRecords(normalizedZone);
        LOGGER.debug("Retrieved {} DNS records for zone '{}'", records.size(), normalizedZone);

        return records;
    }

    @Override
    public void upsertSingleRecord(String zoneFqdn, String recordFqdn, DnsRecordType type, long ttlSeconds, String content) {
        String zone = DnsName.normalizeZone(zoneFqdn);
        String name = DnsName.normalizeFqdn(recordFqdn);

        LOGGER.debug("Upserting DNS record. zone='{}', name='{}', type='{}', ttlSeconds={}", zone, name, type, ttlSeconds);

        DnsRecord record = new DnsRecord(name, type, ttlSeconds, content, false);

        DnsClient client = resolver.resolveByZone(zone);
        LOGGER.debug("Resolved DNS client '{}' for zone '{}'", client.getClass().getSimpleName(), zone);

        DnsRrsetKey rrsetKey = new DnsRrsetKey(name, type);
        LOGGER.debug("Replacing RRSet. zone='{}', rrsetKey='{}', ttlSeconds={}", zone, rrsetKey, ttlSeconds);

        client.replaceRrset(zone, rrsetKey, ttlSeconds, List.of(record));
        LOGGER.debug("Successfully upserted DNS record. zone='{}', name='{}', type='{}'", zone, name, type);
    }

    @Override
    public void deleteRrset(String zoneFqdn, String recordFqdn, DnsRecordType type) {
        String zone = DnsName.normalizeZone(zoneFqdn);
        String name = DnsName.normalizeFqdn(recordFqdn);

        LOGGER.debug("Deleting DNS RRSet. zone='{}', name='{}', type='{}'", zone, name, type);

        DnsClient client = resolver.resolveByZone(zone);
        LOGGER.debug("Resolved DNS client '{}' for zone '{}'", client.getClass().getSimpleName(), zone);

        DnsRrsetKey rrsetKey = new DnsRrsetKey(name, type);
        LOGGER.debug("Deleting RRSet. zone='{}', rrsetKey='{}'", zone, rrsetKey);

        client.deleteRrset(zone, rrsetKey);
        LOGGER.debug("Successfully deleted DNS RRSet. zone='{}', name='{}', type='{}'", zone, name, type);
    }

    @Override
    public boolean recordExists(String zoneFqdn, String recordFqdn, DnsRecordType type) {
        String zone = DnsName.normalizeZone(zoneFqdn);
        String name = DnsName.normalizeFqdn(recordFqdn);

        LOGGER.debug("Checking if DNS record exists. zone='{}', name='{}', type='{}'", zone, name, type);

        DnsClient client = resolver.resolveByZone(zone);
        List<DnsRecord> records = client.listRecords(zone);

        boolean exists = records.stream()
                .anyMatch(record -> record.fqdn().equals(name) && record.type() == type);

        LOGGER.debug("DNS record existence check result. zone='{}', name='{}', type='{}', exists={}",
                zone, name, type, exists);

        return exists;
    }

    @Override
    public Optional<DnsRecord> findRecord(String zoneFqdn, String recordFqdn, DnsRecordType type) {
        String zone = DnsName.normalizeZone(zoneFqdn);
        String name = DnsName.normalizeFqdn(recordFqdn);

        LOGGER.debug("Finding DNS record. zone='{}', name='{}', type='{}'", zone, name, type);

        DnsClient client = resolver.resolveByZone(zone);
        List<DnsRecord> records = client.listRecords(zone);

        Optional<DnsRecord> result = records.stream()
                .filter(record -> record.fqdn().equals(name) && record.type() == type)
                .findFirst();

        if (result.isPresent()) {
            LOGGER.debug("Found DNS record. zone='{}', name='{}', type='{}'", zone, name, type);
        } else {
            LOGGER.debug("DNS record not found. zone='{}', name='{}', type='{}'", zone, name, type);
        }

        return result;
    }
}
