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

import io.maestro3.agent.network.domain.DnsRecord;
import io.maestro3.agent.network.domain.DnsRecordType;

import java.util.List;

/**
 * @author Serhii Akhmetshin
 * Created: 17/12/2025
 */
public interface IDnsRecordService {
    List<DnsRecord> listZoneRecords(String zoneFqdn);

    void upsertSingleRecord(String zoneFqdn, String recordFqdn, DnsRecordType type, long ttlSeconds, String content);

    void deleteRrset(String zoneFqdn, String recordFqdn, DnsRecordType type);

    /**
     * Checks if a specific DNS record exists in the zone.
     *
     * @param zoneFqdn   the fully qualified domain name of the zone
     * @param recordFqdn the fully qualified domain name of the record to check
     * @param type       the DNS record type
     * @return true if the record exists, false otherwise
     */
    boolean recordExists(String zoneFqdn, String recordFqdn, DnsRecordType type);

    /**
     * Finds a specific DNS record in the zone if it exists.
     *
     * @param zoneFqdn   the fully qualified domain name of the zone
     * @param recordFqdn the fully qualified domain name of the record to find
     * @param type       the DNS record type
     * @return Optional containing the matching DnsRecord if found, empty otherwise
     */
    java.util.Optional<DnsRecord> findRecord(String zoneFqdn, String recordFqdn, DnsRecordType type);
}
