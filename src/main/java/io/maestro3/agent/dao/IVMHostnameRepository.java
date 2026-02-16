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

package io.maestro3.agent.dao;

import io.maestro3.agent.network.model.HostnameStatus;
import io.maestro3.agent.network.model.VMHostnameMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for VM hostname mappings.
 */
@Repository
public interface IVMHostnameRepository extends MongoRepository<VMHostnameMapping, String> {

    /**
     * Find mapping by VM ID.
     */
    VMHostnameMapping findByVmId(String vmId);

    /**
     * Find mapping by hostname and tenant (for uniqueness check).
     */
    VMHostnameMapping findByHostnameAndTenantIdAndCloudId(String hostname, String tenantId, String cloudId);

    /**
     * Find all mappings for a tenant and cloud.
     */
    List<VMHostnameMapping> findByTenantIdAndCloudId(String tenantId, String cloudId);

    /**
     * Find mappings by status.
     */
    List<VMHostnameMapping> findByStatus(HostnameStatus status);

    /**
     * Find stale RESERVED mappings (for cleanup).
     */
    List<VMHostnameMapping> findByStatusAndCreatedDateBefore(HostnameStatus status, long date);

    /**
     * Delete mapping by VM ID.
     */
    void deleteByVmId(String vmId);

    /**
     * Check if hostname exists for tenant.
     */
    boolean existsByHostnameAndTenantIdAndCloudId(String hostname, String tenantId, String cloudId);

    /**
     * Delete expired reservations.
     */
    void deleteByStatusAndExpiresAtBefore(HostnameStatus status, long date);

    /**
     * Find expired reservations for cleanup.
     */
    List<VMHostnameMapping> findByStatusAndExpiresAtBefore(HostnameStatus status, long date);
}

