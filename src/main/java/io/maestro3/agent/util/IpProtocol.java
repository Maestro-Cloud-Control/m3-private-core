/*
 * Copyright 2023 Maestro Cloud Control LLC
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

package io.maestro3.agent.util;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;


public enum IpProtocol {

    @SerializedName("tcp") TCP(6),
    @SerializedName("udp") UDP(17),
    @SerializedName("icmp") ICMP(1);

    private static final Set<IpProtocol> ALL_PROTOCOLS = Sets.newHashSet(TCP, UDP);

    private final int protocolNumber;

    IpProtocol(int protocolNumber) {
        this.protocolNumber = protocolNumber;
    }

    public int getProtocolNumber() {
        return protocolNumber;
    }

    public String decode() {
        return this.name().toLowerCase();
    }

    public static IpProtocol fromName(String protocol) {
        return Stream.of(values())
            .filter(ipProtocol -> StringUtils.equalsIgnoreCase(ipProtocol.name(), protocol))
            .findAny()
            .orElse(null);
    }

    public static IpProtocol fromNumber(String number) {
        return Stream.of(values())
            .filter(ipProtocol -> StringUtils.equals(number, String.valueOf(ipProtocol.getProtocolNumber())))
            .findAny()
            .orElse(null);
    }

    public static Set<IpProtocol> getAllProtocols() {
        return Collections.unmodifiableSet(ALL_PROTOCOLS);
    }

    public boolean in(IpProtocol... ipProtocols) {
        if (ipProtocols == null || ipProtocols.length == 0) {
            return false;
        }
        for (IpProtocol protocol : ipProtocols) {
            if (this.equals(protocol)) {
                return true;
            }
        }
        return false;
    }
}
