package io.maestro3.agent.terraform.sevice;

import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.platform.model.PlatformServiceDefinition;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;

import java.util.Collection;
import java.util.Optional;

public interface IPlatformServiceDefinitionService {
    boolean isServiceExist(String serviceName);

    void save(PlatformServiceDefinition serviceDefinition);

    Optional<PlatformServiceDefinition> findByName(String serviceName);

    Collection<PlatformServiceDefinition> findByTenant(ITenant tenant,
                                                       PlatformServiceDeploymentType deploymentType);

    Optional<PlatformServiceDefinition> findByNameAndTenant(String serviceName, ITenant tenant);

    Optional<PlatformServiceDefinition> findByServiceName(String serviceName,
                                                          PlatformServiceDeploymentType deploymentType);

    Optional<PlatformServiceDefinition> findByNameAndPrivateAgentId(String serviceName, String privateAgentId);

    void delete(PlatformServiceDefinition serviceDefinition);
}
