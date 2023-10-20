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

import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;


public class IpAddressMatcher {

    private final Set<MatchedIp> ipSet;

    public IpAddressMatcher(String... ipAddresses) {
        Assert.notNull(ipAddresses, "IP address is null");
        ipSet = new HashSet<>();
        for (String ipAddress : ipAddresses) {
            int maskBits;
            InetAddress inetAddress;
            if (ipAddress.indexOf('/') > 0) {
                String[] addressAndMask = ipAddress.split("/");
                ipAddress = addressAndMask[0];
                maskBits = Integer.parseInt(addressAndMask[1]);
            } else {
                maskBits = -1;
            }
            inetAddress = parseAddress(ipAddress);
            Assert.isTrue(inetAddress.getAddress().length * 8 >= maskBits,
                String.format("IP address %s is too short for bitmask of length %d", ipAddress, maskBits));
            ipSet.add(new MatchedIp(maskBits, inetAddress));
        }
    }

    public boolean isMatched(String address) {
        for (MatchedIp matchedIp : ipSet) {
            if (matches(address, matchedIp)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(String address, MatchedIp matchedIp) {
        InetAddress remoteAddress = parseAddress(address);

        if (!matchedIp.inetAddress.getClass().equals(remoteAddress.getClass())) {
            return false;
        }

        if (matchedIp.maskBits < 0) {
            return remoteAddress.equals(matchedIp.inetAddress);
        }

        byte[] remAddr = remoteAddress.getAddress();
        byte[] reqAddr = matchedIp.inetAddress.getAddress();

        int maskFullBytes = matchedIp.maskBits / 8;
        byte finalByte = (byte) (0xFF00 >> (matchedIp.maskBits & 0x07));

        for (int i = 0; i < maskFullBytes; i++) {
            if (remAddr[i] != reqAddr[i]) {
                return false;
            }
        }

        if (finalByte != 0) {
            return (remAddr[maskFullBytes] & finalByte) == (reqAddr[maskFullBytes] & finalByte);
        }

        return true;
    }

    private InetAddress parseAddress(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Failed to parse address" + address, e);
        }
    }

    private static class MatchedIp {
        private final int maskBits;
        private final InetAddress inetAddress;

        private MatchedIp(int maskBits, InetAddress inetAddress) {
            this.maskBits = maskBits;
            this.inetAddress = inetAddress;
        }
    }

}
