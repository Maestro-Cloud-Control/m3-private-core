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
package io.maestro3.agent.network.client.powerdns.dto;

import java.util.List;
import java.util.Objects;

public final class PowerDnsZoneRrSet {

    private String name;
    private String type;
    private Long ttl;
    private List<PowerDnsZoneRecord> records;

    public PowerDnsZoneRrSet() {
    }

    public PowerDnsZoneRrSet(String name, String type, Long ttl, List<PowerDnsZoneRecord> records) {
        this.name = name;
        this.type = type;
        this.ttl = ttl;
        this.records = records;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Long getTtl() {
        return ttl;
    }

    public List<PowerDnsZoneRecord> getRecords() {
        return records;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        PowerDnsZoneRrSet that = (PowerDnsZoneRrSet) obj;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.ttl, that.ttl)
                && Objects.equals(this.records, that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, ttl, records);
    }

    @Override
    public String toString() {
        return "PowerDnsZoneRrSet[" +
                "name=" + name + ", " +
                "type=" + type + ", " +
                "ttl=" + ttl + ", " +
                "records=" + records + ']';
    }
}
