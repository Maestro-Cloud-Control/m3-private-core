/*
 * Copyright 2025 Maestro Cloud Control LLC
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
 */
package io.maestro3.agent.network.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.maestro3.agent.network.model.DnsServer;
import io.maestro3.agent.network.service.DnsName;
import io.maestro3.agent.network.service.IDnsServerLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class DnsClientResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DnsClientResolver.class);

    private final IDnsServerLookupService lookupService;
    private final DnsClientFactory factory;
    private static final Cache<String, DnsClient> CACHE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();

    @Autowired
    public DnsClientResolver(IDnsServerLookupService lookupService, DnsClientFactory factory) {
        this.lookupService = lookupService;
        this.factory = factory;
    }

    public DnsClient resolveByZone(String zoneFqdn) {
        String normalizedZone = DnsName.normalizeZone(zoneFqdn);
        LOGGER.info("Resolving DNS client by zone. zoneFqdn='{}', normalizedZone='{}'", zoneFqdn, normalizedZone);

        DnsServer server = lookupService.getServerByZoneOrThrow(normalizedZone);
        LOGGER.info("Resolved DNS server for zone. normalizedZone='{}', serverId='{}', serverName='{}', provider='{}'",
                normalizedZone, server.getId(), server.getName(), server.getProvider());

        try {
            DnsClient client = CACHE.get(server.getId(), () -> {
                LOGGER.info("Cache miss for DNS client. serverId='{}', creating new client instance", server.getId());
                return factory.create(server);
            });
            LOGGER.info("Resolved DNS client from cache/factory. serverId='{}', clientClass='{}', baseUrl='{}'",
                    server.getId(), client.getClass().getSimpleName(), client.getBaseUrl());
            return client;
        } catch (ExecutionException e) {
            LOGGER.error("Failed to resolve DNS client for zone. normalizedZone='{}', serverId='{}'",
                    normalizedZone, server.getId(), e);
            throw new RuntimeException(e);
        }
    }

    public void evictServer(String serverId) {
        if (serverId != null) {
            LOGGER.info("Evicting DNS client from cache. serverId='{}'", serverId);
            CACHE.invalidate(serverId);
        }
    }
}
