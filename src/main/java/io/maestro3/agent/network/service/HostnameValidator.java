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

package io.maestro3.agent.network.service;

import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.dao.IVMHostnameRepository;
import io.maestro3.agent.network.domain.DnsRecordType;
import io.maestro3.sdk.internal.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates hostname format and availability according to RFC 1123.
 */
@Component
public class HostnameValidator {

    private static final Logger LOG = LoggerFactory.getLogger(HostnameValidator.class);

    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?$");
    private static final int MAX_HOSTNAME_LENGTH = 63;
    private static final Set<String> RESERVED_HOSTNAMES = Set.of(
            "localhost", "vm", "api", "admin", "root",
            "gateway", "dns", "mail", "smtp", "www"
    );

    private final IDnsRecordService dnsRecordService;
    private final IVMHostnameRepository hostnameRepository;

    @Autowired
    public HostnameValidator(IDnsRecordService dnsRecordService, IVMHostnameRepository hostnameRepository) {
        this.dnsRecordService = dnsRecordService;
        this.hostnameRepository = hostnameRepository;
    }

    /**
     * Validates hostname format according to RFC 1123.
     *
     * @param hostname hostname to validate
     * @throws ReadableAgentException if hostname is invalid
     */
    public void validateFormat(String hostname) {
        if (StringUtils.isBlank(hostname)) {
            throw new ReadableAgentException("Hostname cannot be empty");
        }

        if (hostname.length() > MAX_HOSTNAME_LENGTH) {
            throw new ReadableAgentException(
                    "Hostname must not exceed " + MAX_HOSTNAME_LENGTH + " characters, got: " + hostname.length());
        }

        String lowercaseHostname = hostname.toLowerCase();
        if (!lowercaseHostname.equals(hostname)) {
            throw new ReadableAgentException(
                    "Hostname must be lowercase. Got: '" + hostname + "', expected: '" + lowercaseHostname + "'");
        }

        if (!HOSTNAME_PATTERN.matcher(hostname).matches()) {
            throw new ReadableAgentException(
                    "Hostname must be 1-63 characters, lowercase alphanumeric with hyphens, " +
                            "not starting/ending with hyphen. Got: '" + hostname + "'");
        }

        if (RESERVED_HOSTNAMES.contains(hostname)) {
            throw new ReadableAgentException("Hostname '" + hostname + "' is reserved and cannot be used");
        }

        if (hostname.startsWith("vm-")) {
            throw new ReadableAgentException(
                    "Hostname cannot start with 'vm-' as this pattern is reserved for auto-generated names");
        }
    }

    /**
     * Checks if hostname is available in DNS zone.
     *
     * @param hostname hostname to check
     * @param dnsZone  DNS zone
     * @throws ReadableAgentException if DNS record already exists
     */
    public void checkDnsAvailability(String hostname, String dnsZone) {
        if (StringUtils.isBlank(dnsZone)) {
            LOG.debug("Skipping DNS availability check - no DNS zone configured");
            return;
        }

        String fullDnsName = hostname + "." + dnsZone;
        try {
            boolean exists = dnsRecordService.recordExists(dnsZone, fullDnsName, DnsRecordType.A);
            if (exists) {
                throw new ReadableAgentException(
                        "DNS record for '" + fullDnsName + "' already exists in zone '" + dnsZone + "'");
            }
        } catch (ReadableAgentException e) {
            throw e;
        } catch (Exception e) {
            LOG.warn("Failed to check DNS availability for hostname '{}' in zone '{}': {}",
                    hostname, dnsZone, e.getMessage());
            // Don't fail VM creation if DNS check fails - proceed with optimistic approach
        }
    }

    /**
     * Checks if hostname is already mapped to another VM.
     *
     * @param hostname hostname to check
     * @param tenantId tenant ID
     * @param cloudId  cloud ID
     * @throws ReadableAgentException if hostname is already mapped
     */
    public void checkMappingAvailability(String hostname, String tenantId, String cloudId) {
        boolean exists = hostnameRepository.existsByHostnameAndTenantIdAndCloudId(hostname, tenantId, cloudId);
        if (exists) {
            throw new ReadableAgentException(
                    "Hostname '" + hostname + "' is already in use in this tenant");
        }
    }

    /**
     * Full validation - format, DNS, and mapping availability.
     *
     * @param hostname hostname to validate
     * @param tenantId tenant ID
     * @param cloudId  cloud ID
     * @param dnsZone  DNS zone
     * @throws ReadableAgentException if validation fails
     */
    public void validate(String hostname, String tenantId, String cloudId, String dnsZone) {
        LOG.debug("Validating hostname '{}' for tenant '{}' in cloud '{}' with DNS zone '{}'",
                hostname, tenantId, cloudId, dnsZone);

        validateFormat(hostname);
        checkMappingAvailability(hostname, tenantId, cloudId);
        checkDnsAvailability(hostname, dnsZone);

        LOG.debug("Hostname '{}' validation passed", hostname);
    }
}
