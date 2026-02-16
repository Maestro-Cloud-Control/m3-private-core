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
package io.maestro3.agent.network.domain;

import java.util.Objects;

public final class DnsRecord {
    private final String fqdn;
    private final DnsRecordType type;
    private final long ttlSeconds;
    private final String content;
    private final boolean disabled;

    public DnsRecord(String fqdn, DnsRecordType type, long ttlSeconds, String content, boolean disabled) {
        Objects.requireNonNull(fqdn, "fqdn");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(content, "content");
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("ttlSeconds must be > 0");
        }
        this.fqdn = fqdn;
        this.type = type;
        this.ttlSeconds = ttlSeconds;
        this.content = content;
        this.disabled = disabled;
    }

    public String fqdn() {
        return fqdn;
    }

    public DnsRecordType type() {
        return type;
    }

    public long ttlSeconds() {
        return ttlSeconds;
    }

    public String content() {
        return content;
    }

    public boolean disabled() {
        return disabled;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DnsRecord) obj;
        return Objects.equals(this.fqdn, that.fqdn) &&
                Objects.equals(this.type, that.type) &&
                this.ttlSeconds == that.ttlSeconds &&
                Objects.equals(this.content, that.content) &&
                this.disabled == that.disabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fqdn, type, ttlSeconds, content, disabled);
    }

    @Override
    public String toString() {
        return "DnsRecord[" +
                "fqdn=" + fqdn + ", " +
                "type=" + type + ", " +
                "ttlSeconds=" + ttlSeconds + ", " +
                "content=" + content + ", " +
                "disabled=" + disabled + ']';
    }

}
