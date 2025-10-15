package io.maestro3.agent.dao;

import io.maestro3.agent.platform.model.PlatformServiceDefinition;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;

import java.util.Collection;
import java.util.Optional;

public interface IPlatformServiceDefinitionDao {
    boolean isServiceDefinitionExist(String serviceName);

    PlatformServiceDefinition save(PlatformServiceDefinition serviceDefinition);

    Optional<PlatformServiceDefinition> findByName(String serviceName);

    Optional<PlatformServiceDefinition> findByNameAndTenant(String serviceName,
                                                            String tenantDisplayName,
                                                            String cloud);

    Collection<PlatformServiceDefinition> findByTenant(String tenantDisplayName, String cloud,
                                                       PlatformServiceDeploymentType deploymentType);

    Optional<PlatformServiceDefinition> find(String serviceName,
                                               PlatformServiceDeploymentType deploymentType);

    void delete(PlatformServiceDefinition serviceDefinition);

    Optional<PlatformServiceDefinition> findByNameAndPrivateAgentId(String serviceName, String privateAgentId);

    void deleteByName(String name);
}
