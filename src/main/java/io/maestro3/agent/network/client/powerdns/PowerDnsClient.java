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
package io.maestro3.agent.network.client.powerdns;

import io.maestro3.agent.network.client.DnsClient;
import io.maestro3.agent.network.client.powerdns.dto.PowerDnsZoneDto;
import io.maestro3.agent.network.client.powerdns.dto.PowerDnsZonePatchRecord;
import io.maestro3.agent.network.client.powerdns.dto.PowerDnsZonePatchRequest;
import io.maestro3.agent.network.client.powerdns.dto.PowerDnsZonePatchRrSet;
import io.maestro3.agent.network.domain.DnsRecord;
import io.maestro3.agent.network.domain.DnsRecordType;
import io.maestro3.agent.network.domain.DnsRrsetKey;
import io.maestro3.agent.network.service.DnsName;
import io.maestro3.sdk.internal.util.JsonUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PowerDnsClient implements DnsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerDnsClient.class);

    private static final String HEADER_API_KEY = "X-API-Key";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String APPLICATION_JSON = "application/json";

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String apiKey;
    private final String serverId;

    public PowerDnsClient(HttpClient httpClient, String baseUrl, String apiKey, String serverId) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey must not be null");
        this.serverId = Objects.requireNonNull(serverId, "serverId must not be null");
        LOGGER.info("Initialized PowerDnsClient. baseUrl='{}', serverId='{}'", this.baseUrl, this.serverId);
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public List<DnsRecord> listRecords(String zoneFqdn) {
        String normalizedZone = DnsName.normalizeZone(zoneFqdn);
        String url = buildListRecordsUrl(normalizedZone);
        LOGGER.info("Listing PowerDNS records. zoneFqdn='{}', normalizedZone='{}', url='{}'", zoneFqdn, normalizedZone, url);

        HttpGet request = new HttpGet(url);
        request.addHeader(HEADER_API_KEY, apiKey);
        request.addHeader(HEADER_ACCEPT, APPLICATION_JSON);

        try {
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.info("Received PowerDNS response for listRecords. normalizedZone='{}', statusCode={}", normalizedZone, statusCode);

            if (statusCode < 200 || statusCode >= 300) {
                String body = readBodySafe(response.getEntity());
                LOGGER.warn("PowerDNS API error on listRecords. normalizedZone='{}', statusCode={}, responseBody='{}'",
                        normalizedZone, statusCode, body);
                throw new PowerDnsException("PowerDNS API error: " + statusCode + " " + body);
            }

            String body = readBodySafe(response.getEntity());
            if (body == null || body.isBlank()) {
                LOGGER.info("PowerDNS listRecords returned empty body. normalizedZone='{}'", normalizedZone);
                return List.of();
            }

            PowerDnsZoneDto zone = JsonUtils.parseJson(body, PowerDnsZoneDto.class);
            if (zone == null || zone.getRrsets() == null) {
                LOGGER.info("PowerDNS listRecords returned no rrsets. normalizedZone='{}'", normalizedZone);
                return List.of();
            }

            List<DnsRecord> records = new ArrayList<>();
            for (var rr : zone.getRrsets()) {
                DnsRecordType type = DnsRecordType.valueOf(rr.getType());
                long ttl = rr.getTtl() == null ? 60 : rr.getTtl();
                if (rr.getRecords() == null) {
                    continue;
                }
                for (var r : rr.getRecords()) {
                    records.add(new DnsRecord(rr.getName(), type, ttl, r.getContent(), Boolean.TRUE.equals(r.getDisabled())));
                }
            }

            LOGGER.info("Mapped PowerDNS records to internal model. normalizedZone='{}', recordCount={}",
                    normalizedZone, records.size());

            return records;
        } catch (ClientProtocolException e) {
            LOGGER.error("PowerDNS client protocol error on listRecords. normalizedZone='{}'", normalizedZone, e);
            throw new PowerDnsException("PowerDNS client protocol error", e);
        } catch (IOException e) {
            LOGGER.error("PowerDNS I/O error on listRecords. normalizedZone='{}'", normalizedZone, e);
            throw new PowerDnsException("PowerDNS I/O error", e);
        }
    }

    @Override
    public void replaceRrset(String zoneFqdn, DnsRrsetKey key, long ttlSeconds, List<DnsRecord> records) {
        String normalizedZone = DnsName.normalizeZone(zoneFqdn);
        String name = DnsName.normalizeFqdn(key.fqdn());

        LOGGER.info("Replacing PowerDNS RRSet. zoneFqdn='{}', normalizedZone='{}', name='{}', type='{}', ttlSeconds={}, recordCount={}",
                zoneFqdn, normalizedZone, name, key.type(), ttlSeconds, records == null ? 0 : records.size());

        PowerDnsZonePatchRequest payload = new PowerDnsZonePatchRequest(List.of(
            new PowerDnsZonePatchRrSet(
                name,
                key.type().name(),
                ttlSeconds,
                "REPLACE",
                records.stream()
                    .map(r -> new PowerDnsZonePatchRecord(r.content(), r.disabled()))
                    .collect(Collectors.toList()),
                List.of()
            )
        ));

        patchZone(normalizedZone, payload);
    }

    @Override
    public void deleteRrset(String zoneFqdn, DnsRrsetKey key) {
        String normalizedZone = DnsName.normalizeZone(zoneFqdn);
        String name = DnsName.normalizeFqdn(key.fqdn());

        LOGGER.info("Deleting PowerDNS RRSet. zoneFqdn='{}', normalizedZone='{}', name='{}', type='{}'",
                zoneFqdn, normalizedZone, name, key.type());

        PowerDnsZonePatchRequest payload = new PowerDnsZonePatchRequest(List.of(
            new PowerDnsZonePatchRrSet(
                name,
                key.type().name(),
                null,
                "DELETE",
                List.of(),
                List.of()
            )
        ));

        patchZone(normalizedZone, payload);
    }

    private void patchZone(String zoneId, PowerDnsZonePatchRequest payload) {
        String url = buildPatchZoneUrl(zoneId);
        LOGGER.info("Patching PowerDNS zone. zoneId='{}', url='{}'", zoneId, url);

        HttpPatch request = new HttpPatch(url);
        request.addHeader(HEADER_API_KEY, apiKey);
        request.addHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON);
        request.addHeader(HEADER_ACCEPT, APPLICATION_JSON);

        String body = JsonUtils.convertObjectToJson(payload);
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

        try {
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.info("Received PowerDNS response for patchZone. zoneId='{}', statusCode={}", zoneId, statusCode);

            if (statusCode < 200 || statusCode >= 300) {
                String responseBody = readBodySafe(response.getEntity());
                LOGGER.warn("PowerDNS API error on patchZone. zoneId='{}', statusCode={}, responseBody='{}'",
                        zoneId, statusCode, responseBody);
                throw new PowerDnsException("PowerDNS API error: " + statusCode + " " + responseBody);
            }
        } catch (ClientProtocolException e) {
            LOGGER.error("PowerDNS client protocol error on patchZone. zoneId='{}'", zoneId, e);
            throw new PowerDnsException("PowerDNS client protocol error", e);
        } catch (IOException e) {
            LOGGER.error("PowerDNS I/O error on patchZone. zoneId='{}'", zoneId, e);
            throw new PowerDnsException("PowerDNS I/O error", e);
        }
    }

    private String buildListRecordsUrl(String normalizedZone) {
        StringBuilder builder = new StringBuilder();
        builder.append(trimTrailingSlash(baseUrl));
        builder.append("/api/v1/servers/");
        builder.append(serverId);
        builder.append("/zones/");
        builder.append(normalizedZone);
        builder.append("?rrsets=true");
        return builder.toString();
    }

    private String buildPatchZoneUrl(String zoneId) {
        StringBuilder builder = new StringBuilder();
        builder.append(trimTrailingSlash(baseUrl));
        builder.append("/api/v1/servers/");
        builder.append(serverId);
        builder.append("/zones/");
        builder.append(zoneId);
        return builder.toString();
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.charAt(value.length() - 1) == '/') {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String readBodySafe(HttpEntity entity) {
        if (entity == null) {
            return null;
        }
        try {
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read PowerDNS response body", e);
        }
    }

    public static class PowerDnsException extends RuntimeException {

        public PowerDnsException(String message) {
            super(message);
        }

        public PowerDnsException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
