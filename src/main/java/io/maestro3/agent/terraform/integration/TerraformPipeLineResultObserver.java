package io.maestro3.agent.terraform.integration;

import io.maestro3.agent.platform.deployment.IPlatformServiceTerraformDeploymentManager;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentInfo;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import io.maestro3.agent.terraform.integration.context.PaasTerraformPipeLineContextExtension;
import io.maestro3.agent.terraform.sevice.IPlatformServiceEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.terraform.integration.IContextExtension;
import team.syndicate.terraform.engine.terraform.integration.TerraformPipeLineContext;
import team.syndicate.terraform.engine.terraform.integration.manager.ITerraformPipeLineResultObserver;

import java.util.Optional;

@Service
public class TerraformPipeLineResultObserver implements ITerraformPipeLineResultObserver {

    private static final Logger LOG = LoggerFactory.getLogger(TerraformPipeLineResultObserver.class);

    private final IPlatformServiceEntryService entryService;
    private final IPlatformServiceTerraformDeploymentManager serviceDeploymentManager;

    @Autowired
    public TerraformPipeLineResultObserver(IPlatformServiceEntryService entryService,
                                           IPlatformServiceTerraformDeploymentManager serviceDeploymentManager) {
        this.entryService = entryService;
        this.serviceDeploymentManager = serviceDeploymentManager;
    }

    @Override
    public void onPipeLineFinished(final TerraformPipeLineContext pipeLineContext, final PipeLineStatus status) {
        final IContextExtension contextExtension = pipeLineContext.getContextExtension();
        if (contextExtension instanceof PaasTerraformPipeLineContextExtension) {
            PaasTerraformPipeLineContextExtension paasContextExtension = (PaasTerraformPipeLineContextExtension) contextExtension;
            processPaasPipeLineResult(paasContextExtension, status);
        }
    }

    private void processPaasPipeLineResult(final PaasTerraformPipeLineContextExtension paasContextExtension, final PipeLineStatus status) {
        final String serviceEntryId = paasContextExtension.getServiceEntryId();
        Optional<PlatformServiceEntry> optionalServiceEntry = entryService.findById(serviceEntryId);
        if (optionalServiceEntry.isEmpty()) {
            LOG.warn("Got execution result for non-existed service entry {}", serviceEntryId);
            return;
        }

        final PlatformServiceEntry serviceEntry = optionalServiceEntry.get();
        final PlatformServiceDeploymentInfo deploymentInfo = serviceEntry.getDeploymentInfo();
        if (deploymentInfo.getProviderType() != PlatformServiceDeploymentType.TERRAFORM) {
            LOG.error("Got unexpected provider type {}", deploymentInfo.getProviderType());
            return;
        }

        serviceDeploymentManager.processTerraformPipeLineResult(serviceEntry, paasContextExtension, status);
    }
}
