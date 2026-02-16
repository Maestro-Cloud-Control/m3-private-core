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

import io.maestro3.agent.factory.CloseableHttpClientFactory;
import io.maestro3.agent.network.client.powerdns.PowerDnsClient;
import io.maestro3.agent.network.model.DnsProvider;
import io.maestro3.agent.network.model.DnsServer;
import io.maestro3.agent.network.security.SecretResolver;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DnsClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DnsClientFactory.class);

    private final SecretResolver secretResolver;

    @Autowired
    public DnsClientFactory(SecretResolver secretResolver) {
        this.secretResolver = secretResolver;
    }

    public DnsClient create(DnsServer server) {
        if (server == null) {
            throw new IllegalArgumentException("server is null");
        }

        LOGGER.info("Creating DNS client. dnsServerId='{}', name='{}', provider='{}', baseUrl='{}'",
                server.getId(), server.getName(), server.getProvider(), server.getBaseUrl());

        if (server.getProvider() == DnsProvider.POWERDNS) {
            String apiKey = secretResolver.resolve(server.getApiKeySecretRef());
            HttpClient httpClient = CloseableHttpClientFactory.getHttpClient(30);

            String serverId = server.getServerId() == null || server.getServerId().isBlank()
                ? "localhost"
                : server.getServerId();

            LOGGER.info("Instantiating PowerDnsClient. dnsServerId='{}', resolvedServerId='{}'", server.getId(), serverId);

            return new PowerDnsClient(httpClient, server.getBaseUrl(), apiKey, serverId);
        }

        LOGGER.error("Unsupported DNS provider. dnsServerId='{}', provider='{}'", server.getId(), server.getProvider());
        throw new IllegalArgumentException("Unsupported provider: " + server.getProvider());
    }
}
