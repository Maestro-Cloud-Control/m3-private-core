package io.maestro3.agent.integration;

import io.maestro3.agent.platform.IPlatformServiceFacade;
import io.maestro3.agent.terraform.manager.ITerraformStackService;
import io.maestro3.agent.terraform.model.TerraformStack;
import io.maestro3.agent.terraform.model.TerraformStackInfoWrapper;
import io.maestro3.agent.terraform.model.TerraformTemplateDto;
import io.maestro3.agent.tf.integration.manager.ITerraformStackInfoManager;
import io.maestro3.agent.tf.integration.model.ITerraformStackInfo;
import io.maestro3.agent.tf.integration.model.ITerraformTemplateInfo;
import io.maestro3.agent.tf.integration.model.TemplateStatus;
import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageInfo;
import io.maestro3.agent.tf.integration.model.TerraformVariable;
import io.maestro3.sdk.internal.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class TerraformStackInfoManager implements ITerraformStackInfoManager {

    private final ITerraformStackService stackService;
    private final IPlatformServiceFacade platformServiceFacade;

    @Autowired
    public TerraformStackInfoManager(ITerraformStackService stackService,
                                     IPlatformServiceFacade platformServiceFacade) {
        this.platformServiceFacade = platformServiceFacade;
        this.stackService = stackService;
    }

    @Override
    public ITerraformStackInfo createStack(final ITerraformTemplateInfo terraformTemplate, String paasUuid) {
        final TerraformTemplateDto terraformTemplateDto = TerraformTemplateDto.builder()
                .withCloud(terraformTemplate.getCloud())
                .withTenantName(terraformTemplate.getTenantName())
                .withTemplateId(terraformTemplate.getTemplateId())
                .withName(terraformTemplate.getName())
                .withTemplateStatus(TemplateStatus.PENDING_PLANNING)
                .withDescription(terraformTemplate.getDescription())
                .withOwner(terraformTemplate.getOwner())
                .withPaasUuid(paasUuid)
                .withProviders(terraformTemplate.getProviders())
                .withStorageInfo(terraformTemplate.getStorageInfo())
                .build();
        final TerraformStack stack = stackService.createStack(terraformTemplateDto);
        return new TerraformStackInfoWrapper(stack);
    }

    @Override
    public Optional<ITerraformStackInfo> getSingleStack(final ITerraformTemplateInfo terraformTemplateInfo) {
        if (terraformTemplateInfo == null) {
            return Optional.empty();
        }

        return stackService.getSingleStack(terraformTemplateInfo.getTemplateId())
                .map(TerraformStackInfoWrapper::new);
    }

    @Override
    public Optional<ITerraformStackInfo> getStack(final String stackId) {
        return stackService.getStack(stackId)
                .map(TerraformStackInfoWrapper::new);
    }

    @Override
    public void updateStackStatus(final ITerraformStackInfo stackInfo, final TemplateStatus newStatus) {
        if (stackInfo == null) {
            return;
        }
        if (stackInfo.getStatus() == newStatus) {
            return;
        }

        stackService.updateStackStatus(stackInfo.getStackId(), newStatus);
        stackInfo.setStatus(newStatus);

        if (StringUtils.isNotBlank(stackInfo.getPaasUuid())) {
            platformServiceFacade.entryStateChanged(stackInfo.getPaasUuid(), newStatus);
        }
    }

    @Override
    public void updateStackVariables(final ITerraformStackInfo stackInfo, final Map<String, TerraformVariable> newVariables) {
        if (stackInfo == null) {
            return;
        }
        if (Objects.equals(newVariables, stackInfo.getVariables())) {
            return;
        }

        stackService.updateStackVariables(stackInfo.getStackId(), newVariables);
        stackInfo.setVariables(newVariables);
    }

    @Override
    public void deleteStack(final ITerraformStackInfo stackInfo) {
        if (stackInfo == null) {
            return;
        }
        if (StringUtils.isNotBlank(stackInfo.getPaasUuid())) {
            platformServiceFacade.deleteEntry(stackInfo.getPaasUuid());
        }
        stackService.delete(stackInfo.getStackId());
    }

    @Override
    public void actualizeStackInfo(final ITerraformStackInfo stackInfo, final ITerraformTemplateInfo templateInfo) {
        actualizeProviders(stackInfo, templateInfo);
        actualizeSubDirectory(stackInfo, templateInfo);
    }

    private void actualizeProviders(final ITerraformStackInfo stackInfo, final ITerraformTemplateInfo templateInfo) {
        final Set<String> templateProviders = templateInfo.getProviders();
        final Set<String> stackProviders = stackInfo.getProviders();
        if (!Objects.equals(templateProviders, stackProviders)) {
            stackService.updateStackProviders(stackInfo.getStackId(), templateProviders);
            stackInfo.setProviders(templateProviders);
        }
    }

    private void actualizeSubDirectory(final ITerraformStackInfo stackInfo, final ITerraformTemplateInfo templateInfo) {
        final TerraformTemplateStorageInfo templateStorageInfo = templateInfo.getStorageInfo();
        if (!templateStorageInfo.getStorageType().isGitBased()) {
            return;
        }

        final Map<String, String> templateStorageParams = templateStorageInfo.getStorageParams();
        final String templateSubDirectory = templateStorageParams.get(TerraformTemplateStorageInfo.Fields.GIT_SUB_DIRECTORY);
        if (!Objects.equals(templateSubDirectory, stackInfo.getSubDirectory())) {
            stackService.updateStackSubDirectory(stackInfo.getStackId(), templateSubDirectory);
            stackInfo.setSubDirectory(templateSubDirectory);
        }
    }
}
