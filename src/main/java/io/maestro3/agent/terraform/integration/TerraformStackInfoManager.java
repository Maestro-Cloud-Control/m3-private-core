package io.maestro3.agent.terraform.integration;

import io.maestro3.agent.platform.IPlatformServiceFacade;
import io.maestro3.agent.terraform.integration.context.PaasTerraformPipeLineContextExtension;
import io.maestro3.agent.terraform.model.TerraformStack;
import io.maestro3.agent.terraform.model.TerraformStackInfoWrapper;
import io.maestro3.agent.terraform.model.TerraformTemplateDto;
import io.maestro3.agent.terraform.sevice.ITerraformStackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.management.interfaces.service.ITerraformEngineStackInfoService;
import team.syndicate.terraform.engine.terraform.integration.IContextExtension;
import team.syndicate.terraform.engine.terraform.integration.model.ITerraformStackInfo;
import team.syndicate.terraform.engine.terraform.integration.model.ITerraformTemplateInfo;
import team.syndicate.terraform.engine.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateStorageInfo;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;
import team.syndicate.terraform.engine.utils.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TerraformStackInfoManager implements ITerraformEngineStackInfoService {

    private final ITerraformStackService stackService;
    private final IPlatformServiceFacade platformServiceFacade;

    @Autowired
    public TerraformStackInfoManager(ITerraformStackService stackService, IPlatformServiceFacade platformServiceFacade) {
        this.stackService = stackService;
        this.platformServiceFacade = platformServiceFacade;
    }

    @Override
    public ITerraformStackInfo createStack(final ITerraformTemplateInfo terraformTemplate,
                                           final IContextExtension contextExtension) {
        final TerraformTemplateDto.Builder terraformTemplateDtoBuilder = TerraformTemplateDto.builder()
                .withCloud(terraformTemplate.getCloud())
                .withTenantName(terraformTemplate.getTenantName())
                .withTemplateId(terraformTemplate.getTemplateId())
                .withName(terraformTemplate.getName())
                .withTemplateStatus(TemplateStatus.PENDING_PLANNING)
                .withDescription(terraformTemplate.getDescription())
                .withOwner(terraformTemplate.getOwner())
                .withProviders(terraformTemplate.getProviders())
                .withStorageInfo(terraformTemplate.getStorageInfo());
        populateWithExtendedInfo(terraformTemplateDtoBuilder, contextExtension);
        final TerraformStack stack = stackService.createStack(terraformTemplateDtoBuilder.build());
        return new TerraformStackInfoWrapper(stack);
    }

    private void populateWithExtendedInfo(final TerraformTemplateDto.Builder dtoBuilder, final IContextExtension contextExtension) {
        if (contextExtension instanceof PaasTerraformPipeLineContextExtension) {
            PaasTerraformPipeLineContextExtension paasContextExtension = (PaasTerraformPipeLineContextExtension) contextExtension;
            dtoBuilder.withServiceEntryId(paasContextExtension.getServiceEntryId());
        }
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
        if (stackInfo instanceof TerraformStackInfoWrapper) {
            TerraformStackInfoWrapper stackInfoWrapper = (TerraformStackInfoWrapper) stackInfo;
            if (StringUtils.isNotBlank((stackInfoWrapper.getServiceEntryId()))) {
                platformServiceFacade.entryStateChanged(stackInfoWrapper.getServiceEntryId(), newStatus);
            }
        }
        stackInfo.setStatus(newStatus);
    }

    @Override
    public void updateStackVariables(final ITerraformStackInfo stackInfo, final Map<String, TerraformUserVariable> newVariables) {
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
        if (stackInfo instanceof TerraformStackInfoWrapper) {
            TerraformStackInfoWrapper stackInfoWrapper = (TerraformStackInfoWrapper) stackInfo;
            if (StringUtils.isNotBlank(stackInfoWrapper.getServiceEntryId())) {
                platformServiceFacade.deleteEntry(stackInfoWrapper.getServiceEntryId());
            }
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

    @Override
    public Collection<ITerraformStackInfo> findByTemplateName(final String tenantName, final String templateName) {
        return stackService.findByTemplateName(tenantName, templateName)
                .stream()
                .map(TerraformStackInfoWrapper::new)
                .map(ITerraformStackInfo.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ITerraformStackInfo> findByTemplateName(final String tenantName, final String templateName, final String stackId) {
        return stackService.findByTemplateName(tenantName, templateName, stackId)
                .map(TerraformStackInfoWrapper::new);
    }
}
