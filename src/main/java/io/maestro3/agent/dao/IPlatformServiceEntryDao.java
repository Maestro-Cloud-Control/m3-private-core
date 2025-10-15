package io.maestro3.agent.dao;

import io.maestro3.agent.platform.model.PlatformServiceEntry;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface IPlatformServiceEntryDao {

    PlatformServiceEntry save(PlatformServiceEntry platformServiceEntry);

    Optional<PlatformServiceEntry> findById(String serviceEntryId);

    Collection<PlatformServiceEntry> findAll();

    Collection<PlatformServiceEntry> find(String regionName, String tenantName, String serviceName, String owner);

    void delete(PlatformServiceEntry serviceEntry);

    Optional<PlatformServiceEntry> findById(String regionName, String tenantName, String cloud,
                                            String serviceEntryId);

    long count(String serviceName);

    void updateServiceEntrySet(String serviceEntryId, Map<String, Object> fieldsToUpdate);

    String concatenateNestedField(String... fields);

    void updateServiceEntryUnset(String serviceEntryId, Collection<String> fieldsToUnset);

    Optional<PlatformServiceEntry> findByAgentServiceEntryId(String agentServiceEntryId);
}
