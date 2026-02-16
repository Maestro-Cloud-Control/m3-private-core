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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class PowerDnsZonePatchComment {

    private String content;
    private String account;

    @JsonProperty("modified_at")
    private Long modifiedAt;

    public PowerDnsZonePatchComment() {
    }

    public PowerDnsZonePatchComment(String content, String account, Long modifiedAt) {
        this.content = content;
        this.account = account;
        this.modifiedAt = modifiedAt;
    }

    public String getContent() {
        return content;
    }

    public String getAccount() {
        return account;
    }

    public Long getModifiedAt() {
        return modifiedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        PowerDnsZonePatchComment that = (PowerDnsZonePatchComment) obj;
        return Objects.equals(this.content, that.content)
                && Objects.equals(this.account, that.account)
                && Objects.equals(this.modifiedAt, that.modifiedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, account, modifiedAt);
    }

    @Override
    public String toString() {
        return "PowerDnsZonePatchComment[" +
                "content=" + content + ", " +
                "account=" + account + ", " +
                "modifiedAt=" + modifiedAt + ']';
    }
}
