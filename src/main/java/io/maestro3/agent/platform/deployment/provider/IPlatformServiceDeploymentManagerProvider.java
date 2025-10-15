package io.maestro3.agent.platform.deployment.provider;

import io.maestro3.agent.platform.deployment.IPlatformServiceDeploymentManager;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;

@FunctionalInterface
public interface IPlatformServiceDeploymentManagerProvider {
    IPlatformServiceDeploymentManager provideDeploymentManager(PlatformServiceDeploymentType deploymentType);
}
