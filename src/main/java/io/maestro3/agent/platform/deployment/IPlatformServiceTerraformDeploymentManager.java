package io.maestro3.agent.platform.deployment;

import io.maestro3.agent.platform.model.PlatformServiceEntry;
import io.maestro3.agent.terraform.integration.context.PaasTerraformPipeLineContextExtension;
import team.syndicate.terraform.engine.terraform.integration.manager.ITerraformPipeLineResultObserver;

public interface IPlatformServiceTerraformDeploymentManager extends IPlatformServiceDeploymentManager {

    void processTerraformPipeLineResult(final PlatformServiceEntry serviceEntry,
                                        final PaasTerraformPipeLineContextExtension contextExtension,
                                        final ITerraformPipeLineResultObserver.PipeLineStatus status);
}
