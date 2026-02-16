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
package io.maestro3.agent.network.service;

public final class DnsName {

    private DnsName() {
    }

    public static String normalizeZone(String zoneFqdn) {
        return normalizeFqdn(zoneFqdn);
    }

    public static String normalizeFqdn(String fqdn) {
        String s = fqdn == null ? "" : fqdn.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("FQDN must not be empty");
        }
        return s.endsWith(".") ? s : (s + ".");
    }
}
