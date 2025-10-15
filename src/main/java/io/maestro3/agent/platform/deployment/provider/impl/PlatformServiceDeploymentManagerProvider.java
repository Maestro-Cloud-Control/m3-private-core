package io.maestro3.agent.platform.deployment.provider.impl;

import io.maestro3.agent.platform.deployment.IPlatformServiceDeploymentManager;
import io.maestro3.agent.platform.deployment.provider.IPlatformServiceDeploymentManagerProvider;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlatformServiceDeploymentManagerProvider implements IPlatformServiceDeploymentManagerProvider {

    private final Map<PlatformServiceDeploymentType, IPlatformServiceDeploymentManager> deploymentManagers;

    @Autowired
    public PlatformServiceDeploymentManagerProvider(List<IPlatformServiceDeploymentManager> deploymentManagerList) {
        this.deploymentManagers = deploymentManagerList.stream()
                .collect(Collectors.toUnmodifiableMap(IPlatformServiceDeploymentManager::getDeploymentType, Function.identity()));
    }

    @Override
    public IPlatformServiceDeploymentManager provideDeploymentManager(final PlatformServiceDeploymentType deploymentType) {
        return Optional.ofNullable(deploymentManagers.get(deploymentType))
                .orElseThrow(() -> new IllegalArgumentException("There is no manager for deployment type " + deploymentType));
    }
}
