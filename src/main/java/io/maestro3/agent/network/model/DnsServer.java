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
package io.maestro3.agent.network.model;

import org.springframework.data.annotation.Id;

public class DnsServer {

    @Id
    private String id;

    private String name;
    private DnsProvider provider;

    private String baseUrl;
    private String serverId; // PowerDNS server id, e.g. "localhost"
    private String apiKeySecretRef;

    private boolean enabled = true;
    private long createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DnsProvider getProvider() {
        return provider;
    }

    public void setProvider(DnsProvider provider) {
        this.provider = provider;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getApiKeySecretRef() {
        return apiKeySecretRef;
    }

    public void setApiKeySecretRef(String apiKeySecretRef) {
        this.apiKeySecretRef = apiKeySecretRef;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
