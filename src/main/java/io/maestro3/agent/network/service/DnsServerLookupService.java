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

import io.maestro3.agent.dao.network.IDnsServerDao;
import io.maestro3.agent.dao.network.IDnsZoneDao;
import io.maestro3.agent.network.model.DnsServer;
import io.maestro3.agent.network.model.DnsZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DnsServerLookupService implements IDnsServerLookupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DnsServerLookupService.class);

    private final IDnsZoneDao zoneDao;
    private final IDnsServerDao serverDao;

    @Autowired
    public DnsServerLookupService(IDnsZoneDao zoneDao, IDnsServerDao serverDao) {
        this.zoneDao = zoneDao;
        this.serverDao = serverDao;
    }

    @Override
    public DnsServer getServerByZoneOrThrow(String zoneFqdn) {
        String normalizedZone = DnsName.normalizeZone(zoneFqdn);
        LOGGER.debug("Looking up DNS server by zone. zoneFqdn='{}', normalizedZone='{}'", zoneFqdn, normalizedZone);

        DnsZone zone = zoneDao.findByZoneFqdn(normalizedZone);
        if (zone == null) {
            LOGGER.warn("DNS zone not found. normalizedZone='{}'", normalizedZone);
            throw new IllegalArgumentException("Zone not found: " + normalizedZone);
        }

        DnsServer server = serverDao.findById(zone.getDnsServerId());
        if (server == null) {
            LOGGER.error("DNS server not found by id. dnsServerId='{}'", zone.getDnsServerId());
            throw new IllegalStateException("DNS server not found by id: " + zone.getDnsServerId());
        }
        if (!server.isEnabled()) {
            LOGGER.warn("DNS server is disabled. dnsServerId='{}', name='{}'", server.getId(), server.getName());
            throw new IllegalStateException("DNS server disabled: " + server.getName());
        }

        LOGGER.debug("Successfully resolved DNS server for zone. normalizedZone='{}', dnsServerId='{}', provider='{}'",
                normalizedZone, server.getId(), server.getProvider());

        return server;
    }
}
