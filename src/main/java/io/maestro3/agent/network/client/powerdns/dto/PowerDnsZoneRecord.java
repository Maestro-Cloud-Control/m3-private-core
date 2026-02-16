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

import java.util.Objects;

public final class PowerDnsZoneRecord {

    private String content;
    private Boolean disabled;

    public PowerDnsZoneRecord() {
    }

    public PowerDnsZoneRecord(String content, Boolean disabled) {
        this.content = content;
        this.disabled = disabled;
    }

    public String getContent() {
        return content;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        PowerDnsZoneRecord that = (PowerDnsZoneRecord) obj;
        return Objects.equals(this.content, that.content)
                && Objects.equals(this.disabled, that.disabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, disabled);
    }

    @Override
    public String toString() {
        return "PowerDnsZoneRecord[" +
                "content=" + content + ", " +
                "disabled=" + disabled + ']';
    }
}
