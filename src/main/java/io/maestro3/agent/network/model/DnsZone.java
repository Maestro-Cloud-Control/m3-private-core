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
package io.maestro3.agent.network.model;

import org.springframework.data.annotation.Id;

/**
 * Zone-to-server mapping.
 */
public class DnsZone {

    @Id
    private String id;

    /**
     * Zone FQDN, recommended normalized with trailing dot: example.org.
     */
    private String zoneFqdn;

    /**
     * Reference to DnsServer.id
     */
    private String dnsServerId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getZoneFqdn() {
        return zoneFqdn;
    }

    public void setZoneFqdn(String zoneFqdn) {
        this.zoneFqdn = zoneFqdn;
    }

    public String getDnsServerId() {
        return dnsServerId;
    }

    public void setDnsServerId(String dnsServerId) {
        this.dnsServerId = dnsServerId;
    }
}
