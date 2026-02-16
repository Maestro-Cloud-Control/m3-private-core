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
import io.maestro3.agent.network.model.HostnameStatus;
import io.maestro3.agent.network.model.VMHostnameMapping;
import io.maestro3.sdk.internal.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Manages hostname lifecycle and mappings.
 */
@Service
public class HostnameService {

    private static final Logger LOG = LoggerFactory.getLogger(HostnameService.class);
    private static final long DNS_TTL = 300L;
    private static final int BOOKING_DURATION_MINUTES = 5;

    private final IVMHostnameRepository hostnameRepository;
    private final IDnsRecordService dnsRecordService;
    private final HostnameValidator hostnameValidator;

    @Autowired
    public HostnameService(IVMHostnameRepository hostnameRepository,
                           IDnsRecordService dnsRecordService,
                           HostnameValidator hostnameValidator) {
        this.hostnameRepository = hostnameRepository;
        this.dnsRecordService = dnsRecordService;
        this.hostnameValidator = hostnameValidator;
    }

    /**
     * Book a hostname temporarily (thread-safe).
     * Creates a RESERVED mapping with expiration time to prevent concurrent conflicts.
     * The booking will expire after 5 minutes if not confirmed.
     *
     * @param hostname custom hostname
     * @param tenantId tenant ID
     * @param cloudId  cloud ID
     * @param dnsZone  DNS zone
     * @return booking ID (mapping ID)
     * @throws ReadableAgentException if hostname is already booked or invalid
     */
    public String bookHostname(String hostname, String tenantId, String cloudId, String dnsZone) {
        LOG.info("Booking hostname '{}' for tenant '{}', cloud '{}' with {}min expiration",
                hostname, tenantId, cloudId, BOOKING_DURATION_MINUTES);

        hostnameValidator.validate(hostname, tenantId, cloudId, dnsZone);

        long updatedDate = System.currentTimeMillis();
        long expiresAt = updatedDate + (BOOKING_DURATION_MINUTES * 60 * 1000L);

        VMHostnameMapping booking = new VMHostnameMapping();
        booking.setVmId(null); // Will be set on confirmation
        booking.setHostname(hostname);
        booking.setFullDnsName(hostname + "." + dnsZone);
        booking.setTenantId(tenantId);
        booking.setCloudId(cloudId);
        booking.setDnsZone(dnsZone);
        booking.setStatus(HostnameStatus.RESERVED);
        booking.setCreatedDate(updatedDate);
        booking.setUpdatedDate(updatedDate);
        booking.setExpiresAt(expiresAt);

        try {
            VMHostnameMapping saved = hostnameRepository.save(booking);
            LOG.info("Booked hostname '{}' with booking ID '{}', expires at '{}'",
                    hostname, saved.getId(), expiresAt);
            return saved.getId();
        } catch (DuplicateKeyException e) {
            // Another thread booked this hostname first - check if it's expired
            VMHostnameMapping existing = hostnameRepository.findByHostnameAndTenantIdAndCloudId(
                    hostname, tenantId, cloudId);

            if (existing != null && existing.getExpiresAt() != 0
                    && existing.getExpiresAt() < updatedDate) {
                // Booking expired - delete and retry
                LOG.info("Found expired booking for hostname '{}', deleting and retrying", hostname);
                hostnameRepository.deleteById(existing.getId());
                return bookHostname(hostname, tenantId, cloudId, dnsZone);
            }

            throw new ReadableAgentException(
                    "Hostname '" + hostname + "' is already booked or in use");
        }
    }

    /**
     * Confirm hostname booking by associating it with a VM.
     * Removes expiration and links booking to VM.
     *
     * @param bookingId booking ID from bookHostname()
     * @param vmId      VM ID
     * @throws ReadableAgentException if booking not found or expired
     */
    public void confirmHostnameBooking(String bookingId, String vmId) {
        VMHostnameMapping booking = hostnameRepository.findById(bookingId).orElse(null);

        if (booking == null) {
            throw new ReadableAgentException("Hostname booking '" + bookingId + "' not found");
        }

        long now = System.currentTimeMillis();
        if (booking.getExpiresAt() != 0 && booking.getExpiresAt() < now) {
            hostnameRepository.deleteById(bookingId);
            throw new ReadableAgentException(
                    "Hostname booking '" + bookingId + "' has expired");
        }

        LOG.info("Confirming hostname booking '{}' for VM '{}', hostname '{}'",
                bookingId, vmId, booking.getHostname());

        booking.setVmId(vmId);
        booking.setExpiresAt(0); // Remove expiration
        booking.setUpdatedDate(now);
        hostnameRepository.save(booking);

        LOG.info("Confirmed hostname '{}' for VM '{}'", booking.getHostname(), vmId);
    }

    /**
     * Cancel hostname booking (on VM creation failure).
     *
     * @param bookingId booking ID
     */
    public void cancelHostnameBooking(String bookingId) {
        if (StringUtils.isBlank(bookingId)) {
            return;
        }

        VMHostnameMapping booking = hostnameRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            LOG.info("Canceling hostname booking '{}' for hostname '{}'",
                    bookingId, booking.getHostname());
            hostnameRepository.deleteById(bookingId);
        }
    }

    /**
     * Activate hostname by creating DNS record.
     * Updates status from RESERVED to ACTIVE.
     *
     * @param vmId      VM ID
     * @param ipAddress VM IP address
     */
    public void activateHostname(String vmId, String ipAddress) {
        VMHostnameMapping mapping = hostnameRepository.findByVmId(vmId);
        if (mapping == null) {
            LOG.debug("No hostname mapping found for VM '{}'", vmId);
            return;
        }

        if (mapping.getStatus() == HostnameStatus.ACTIVE) {
            LOG.debug("Hostname '{}' for VM '{}' is already active", mapping.getHostname(), vmId);
            return;
        }

        if (StringUtils.isBlank(ipAddress)) {
            LOG.warn("Cannot activate hostname '{}' for VM '{}' - IP address is blank",
                    mapping.getHostname(), vmId);
            return;
        }

        LOG.info("Activating hostname '{}' for VM '{}' with IP '{}'",
                mapping.getHostname(), vmId, ipAddress);

        try {
            dnsRecordService.upsertSingleRecord(
                    mapping.getDnsZone(),
                    mapping.getFullDnsName(),
                    DnsRecordType.A,
                    DNS_TTL,
                    ipAddress
            );

            mapping.setStatus(HostnameStatus.ACTIVE);
            long now = System.currentTimeMillis();
            mapping.setActivatedDate(now);
            mapping.setUpdatedDate(now);
            mapping.setCurrentIpAddress(ipAddress);
            hostnameRepository.save(mapping);

            LOG.info("Activated hostname '{}' for VM '{}' - DNS record created: {} -> {}",
                    mapping.getHostname(), vmId, mapping.getFullDnsName(), ipAddress);
        } catch (Exception e) {
            LOG.error("Failed to activate hostname '{}' for VM '{}': {}",
                    mapping.getHostname(), vmId, e.getMessage(), e);
        }
    }

    /**
     * Release hostname when VM is terminated.
     * Deletes DNS record and marks mapping as RELEASED.
     *
     * @param vmId VM ID
     */
    public void releaseHostname(String vmId) {
        VMHostnameMapping mapping = hostnameRepository.findByVmId(vmId);
        if (mapping == null) {
            LOG.debug("No hostname mapping found for VM '{}' to release", vmId);
            return;
        }

        LOG.info("Releasing hostname '{}' for VM '{}'", mapping.getHostname(), vmId);

        if (mapping.getStatus() == HostnameStatus.ACTIVE && StringUtils.isNotBlank(mapping.getDnsZone())) {
            try {
                dnsRecordService.deleteRrset(mapping.getDnsZone(), mapping.getFullDnsName(), DnsRecordType.A);
                LOG.info("Deleted DNS record for hostname '{}' from zone '{}'",
                        mapping.getFullDnsName(), mapping.getDnsZone());
            } catch (Exception e) {
                LOG.error("Failed to delete DNS record for hostname '{}': {}",
                        mapping.getFullDnsName(), e.getMessage(), e);
            }
        }

        hostnameRepository.deleteByVmId(vmId);
        LOG.info("Released hostname '{}' for VM '{}' - mapping deleted", mapping.getHostname(), vmId);
    }

    /**
     * Get hostname mapping for a VM.
     *
     * @param vmId VM ID
     * @return Optional containing mapping if exists
     */
    public Optional<VMHostnameMapping> getHostnameByVmId(String vmId) {
        return Optional.ofNullable(hostnameRepository.findByVmId(vmId));
    }

    /**
     * Update IP address for hostname (if VM IP changes).
     *
     * @param vmId         VM ID
     * @param newIpAddress new IP address
     */
    public void updateIpAddress(String vmId, String newIpAddress) {
        VMHostnameMapping mapping = hostnameRepository.findByVmId(vmId);
        if (mapping == null || mapping.getStatus() != HostnameStatus.ACTIVE) {
            return;
        }

        if (StringUtils.isBlank(newIpAddress)) {
            LOG.warn("Cannot update IP for hostname '{}' - new IP is blank", mapping.getHostname());
            return;
        }

        if (newIpAddress.equals(mapping.getCurrentIpAddress())) {
            LOG.debug("IP address for hostname '{}' unchanged: {}", mapping.getHostname(), newIpAddress);
            return;
        }

        LOG.info("Updating IP address for hostname '{}' from '{}' to '{}'",
                mapping.getHostname(), mapping.getCurrentIpAddress(), newIpAddress);

        try {
            dnsRecordService.upsertSingleRecord(
                    mapping.getDnsZone(),
                    mapping.getFullDnsName(),
                    DnsRecordType.A,
                    DNS_TTL,
                    newIpAddress
            );

            mapping.setCurrentIpAddress(newIpAddress);
            mapping.setUpdatedDate(System.currentTimeMillis());
            hostnameRepository.save(mapping);

            LOG.info("Updated DNS record for hostname '{}': {} -> {}",
                    mapping.getFullDnsName(), mapping.getCurrentIpAddress(), newIpAddress);
        } catch (Exception e) {
            LOG.error("Failed to update IP for hostname '{}': {}",
                    mapping.getHostname(), e.getMessage(), e);
        }
    }

    /**
     * Create DNS record for VM (either custom hostname or IP-based).
     * This method encapsulates the logic of determining which DNS type to create.
     *
     * @param vmId          VM ID
     * @param ipAddress     VM IP address
     * @param tenantDnsZone tenant DNS zone
     * @return DNS address that was created/updated
     */
    public String createDnsForVm(String vmId, String ipAddress, String tenantDnsZone) {
        if (StringUtils.isBlank(ipAddress)) {
            LOG.debug("Cannot create DNS for VM '{}' - IP address is blank", vmId);
            return null;
        }

        if (StringUtils.isBlank(tenantDnsZone)) {
            LOG.debug("Cannot create DNS for VM '{}' - DNS zone is blank", vmId);
            return null;
        }

        // Check if VM has custom hostname
        VMHostnameMapping mapping = hostnameRepository.findByVmId(vmId);
        if (mapping != null) {
            LOG.info("VM '{}' has custom hostname '{}', creating custom DNS",
                    vmId, mapping.getHostname());
            activateHostname(vmId, ipAddress);
            return mapping.getFullDnsName().endsWith(".")
                    ? mapping.getFullDnsName().substring(0, mapping.getFullDnsName().length() - 1)
                    : mapping.getFullDnsName();
        }

        // IP-based DNS
        String ipBasedDns = "vm-" + ipAddress.replace('.', '-') + "." + tenantDnsZone;
        LOG.info("VM '{}' has no custom hostname, creating IP-based DNS: '{}'", vmId, ipBasedDns);

        try {
            dnsRecordService.upsertSingleRecord(
                    tenantDnsZone,
                    ipBasedDns,
                    DnsRecordType.A,
                    DNS_TTL,
                    ipAddress
            );
            return ipBasedDns.endsWith(".")
                    ? ipBasedDns.substring(0, ipBasedDns.length() - 1)
                    : ipBasedDns;
        } catch (Exception e) {
            LOG.error("Failed to create IP-based DNS for VM '{}': {}", vmId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Delete DNS record for VM (either custom hostname or IP-based).
     *
     * @param vmId          VM ID
     * @param ipAddress     VM IP address (for IP-based DNS)
     * @param tenantDnsZone tenant DNS zone
     */
    public void deleteDnsForVm(String vmId, String ipAddress, String tenantDnsZone) {
        // Check if VM has custom hostname
        VMHostnameMapping mapping = hostnameRepository.findByVmId(vmId);
        if (mapping != null) {
            LOG.info("VM '{}' has custom hostname '{}', releasing it", vmId, mapping.getHostname());
            releaseHostname(vmId);
            return;
        }

        // IP-based DNS deletion
        if (StringUtils.isBlank(ipAddress) || StringUtils.isBlank(tenantDnsZone)) {
            LOG.debug("Cannot delete IP-based DNS for VM '{}' - IP or zone is blank", vmId);
            return;
        }

        String ipBasedDns = "vm-" + ipAddress.replace('.', '-') + "." + tenantDnsZone;
        LOG.info("VM '{}' has no custom hostname, deleting IP-based DNS: '{}'", vmId, ipBasedDns);

        try {
            dnsRecordService.deleteRrset(tenantDnsZone, ipBasedDns, DnsRecordType.A);
            LOG.info("Deleted IP-based DNS '{}' for VM '{}'", ipBasedDns, vmId);
        } catch (Exception e) {
            LOG.error("Failed to delete IP-based DNS for VM '{}': {}", vmId, e.getMessage(), e);
        }
    }

    /**
     * Clean up expired bookings (should be called periodically).
     *
     * @return number of expired bookings cleaned up
     */
    public int cleanupExpiredBookings() {
        List<VMHostnameMapping> expired = hostnameRepository.findByStatusAndExpiresAtBefore(
                HostnameStatus.RESERVED, System.currentTimeMillis());

        if (expired.isEmpty()) {
            return 0;
        }

        LOG.info("Found {} expired hostname bookings to clean up", expired.size());

        for (VMHostnameMapping booking : expired) {
            LOG.info("Deleting expired booking: hostname='{}', bookingId='{}', createdDate='{}'",
                    booking.getHostname(), booking.getId(), booking.getCreatedDate());
            hostnameRepository.deleteById(booking.getId());
        }

        return expired.size();
    }
}

