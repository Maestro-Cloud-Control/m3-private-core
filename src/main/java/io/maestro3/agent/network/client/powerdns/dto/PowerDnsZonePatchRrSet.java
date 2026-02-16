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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PowerDnsZonePatchRrSet {

    private String name;
    private String type;
    private Long ttl;
    private String changetype;
    private List<PowerDnsZonePatchRecord> records;
    private List<PowerDnsZonePatchComment> comments;

    public PowerDnsZonePatchRrSet() {
    }

    public PowerDnsZonePatchRrSet(
            String name,
            String type,
            Long ttl,
            String changetype,
            List<PowerDnsZonePatchRecord> records,
            List<PowerDnsZonePatchComment> comments
    ) {
        this.name = name;
        this.type = type;
        this.ttl = ttl;
        this.changetype = changetype;
        this.records = records;
        this.comments = comments;
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

    public String getChangetype() {
        return changetype;
    }

    public List<PowerDnsZonePatchRecord> getRecords() {
        return records;
    }

    public List<PowerDnsZonePatchComment> getComments() {
        return comments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        PowerDnsZonePatchRrSet that = (PowerDnsZonePatchRrSet) obj;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.ttl, that.ttl)
                && Objects.equals(this.changetype, that.changetype)
                && Objects.equals(this.records, that.records)
                && Objects.equals(this.comments, that.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, ttl, changetype, records, comments);
    }

    @Override
    public String toString() {
        return "PowerDnsZonePatchRrSet[" +
                "name=" + name + ", " +
                "type=" + type + ", " +
                "ttl=" + ttl + ", " +
                "changetype=" + changetype + ", " +
                "records=" + records + ", " +
                "comments=" + comments + ']';
    }
}
