package io.maestro3.agent.platform.deployment.impl;

import io.maestro3.agent.dao.IRegionRepository;
import io.maestro3.agent.dao.ITenantRepository;
import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.platform.deployment.IPlatformServiceTerraformDeploymentManager;
import io.maestro3.agent.platform.model.PlatformServiceDefinition;
import io.maestro3.agent.platform.model.PlatformServiceDefinitionInfo;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentInfo;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import io.maestro3.agent.platform.model.PlatformServiceEntryInfo;
import io.maestro3.agent.platform.model.PlatformServiceEntryState;
import io.maestro3.agent.terraform.integration.context.PaasTerraformPipeLineContextExtension;
import io.maestro3.agent.terraform.model.PlatformServiceTask;
import io.maestro3.agent.terraform.model.PlatformServiceVariable;
import io.maestro3.agent.terraform.model.TerraformStack;
import io.maestro3.agent.terraform.model.TerraformStackInfoWrapper;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.model.TerraformTemplateInfoWrapper;
import io.maestro3.agent.terraform.sevice.IPlatformServiceDefinitionService;
import io.maestro3.agent.terraform.sevice.IPlatformServiceEntryService;
import io.maestro3.agent.terraform.sevice.ITerraformStackService;
import io.maestro3.agent.terraform.sevice.ITerraformTemplateService;
import io.maestro3.agent.terraform.util.TerraformVariableUtils;
import io.maestro3.agent.terraform.variable.ITerraformVariableGenerationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.management.model.internal.TenantInfo;
import team.syndicate.terraform.engine.management.paas.IPlatformServiceSensitiveVariableManager;
import team.syndicate.terraform.engine.management.paas.IPlatformServiceTerraformTemplateStorageManager;
import team.syndicate.terraform.engine.terraform.integration.IContextExtension;
import team.syndicate.terraform.engine.terraform.integration.manager.ITerraformPipeLineResultObserver;
import team.syndicate.terraform.engine.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateStorageInfo;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateStorageType;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateVariable;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;
import team.syndicate.terraform.engine.terraform.variable.prefix.IInputFilterVariablePrefixHandler;
import team.syndicate.terraform.engine.utils.CollectionUtils;
import team.syndicate.terraform.engine.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlatformServiceTerraformDeploymentManager extends AbstractPlatformServiceSupportVariableGenerationManager
        implements IPlatformServiceTerraformDeploymentManager {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformServiceTerraformDeploymentManager.class);

    private final IRegionRepository regionRepository;
    private final ITenantRepository tenantService;
    private final ITerraformTemplateService terraformTemplateService;
    private final ITerraformStackService terraformStackService;
    private final IPlatformServiceTerraformTemplateStorageManager paasStorageManager;
    private final IPlatformServiceEntryService entryService;
    private final IPlatformServiceDefinitionService definitionService;
    private final IPlatformServiceSensitiveVariableManager sensitiveVariableManager;

    @Autowired
    public PlatformServiceTerraformDeploymentManager(ITenantRepository tenantService, IRegionRepository regionRepository,
                                                     ITerraformTemplateService terraformTemplateService,
                                                     ITerraformStackService terraformStackService,
                                                     IPlatformServiceTerraformTemplateStorageManager paasStorageManager,
                                                     IPlatformServiceEntryService entryService,
                                                     IPlatformServiceDefinitionService definitionService,
                                                     IPlatformServiceSensitiveVariableManager sensitiveVariableManager,
                                                     ITerraformVariableGenerationHandler variableGenerationHandler,
                                                     List<IInputFilterVariablePrefixHandler> inputFilterHandlers) {
        super(variableGenerationHandler, inputFilterHandlers);
        this.regionRepository = regionRepository;
        this.tenantService = tenantService;
        this.terraformTemplateService = terraformTemplateService;
        this.terraformStackService = terraformStackService;
        this.paasStorageManager = paasStorageManager;
        this.entryService = entryService;
        this.definitionService = definitionService;
        this.sensitiveVariableManager = sensitiveVariableManager;
    }

    @Override
    public PlatformServiceDeploymentInfo handleServiceRegistration(final String serviceName, final Map<String, String> deploymentParams) {
        final String tenantDisplayName = deploymentParams.get(PlatformServiceDeploymentInfo.Fields.TENANT_DISPLAY_NAME);
        final String cloud = deploymentParams.get(PlatformServiceDeploymentInfo.Fields.CLOUD);
        final String regionName = deploymentParams.get(PlatformServiceDeploymentInfo.Fields.REGION_NAME);
        final String templateName = deploymentParams.get(PlatformServiceDeploymentInfo.Fields.TEMPLATE_NAME);

        IRegion region = regionRepository.findByAliasInCloud(regionName, cloud);
        final ITenant tenant = tenantService.findByTenantAliasAndRegionId(tenantDisplayName, region.getId());
        final TerraformTemplate terraformTemplate = terraformTemplateService.find(tenant.getName(), templateName)
                .orElseThrow(() -> new RuntimeException("Terraform template not found " + templateName));
        validateTemplateStatus(terraformTemplate);

        final TerraformTemplateInfoWrapper templateInfo = new TerraformTemplateInfoWrapper(terraformTemplate);
        paasStorageManager.copyTemplateAsPaasBase(templateInfo, serviceName);
        final TerraformTemplate systemPaasTemplate = new TerraformTemplate()
                .withName(terraformTemplate.getName())
                .withDescription(terraformTemplate.getDescription())
                .withStatus(TemplateStatus.VALID)
                .withFormat(terraformTemplate.getFormat())
                .withOwner(terraformTemplate.getOwner())
                .withMultiStack(true)
                .withProviders(terraformTemplate.getProviders())
                .withTemplateVariables(terraformTemplate.getTemplateVariables())
                .withVariables(terraformTemplate.getVariables())
                .withStorageInfo(new TerraformTemplateStorageInfo().withStorageType(TerraformTemplateStorageType.INTERNAL_STORAGE))
                .withSystem(true);
        sensitiveVariableManager.copySecrets(templateInfo, systemPaasTemplate.getTemplateId());
        terraformTemplateService.save(systemPaasTemplate);

        return new PlatformServiceDeploymentInfo()
                .withProviderType(PlatformServiceDeploymentType.TERRAFORM)
                .withProviderParams(Map.of(PlatformServiceDeploymentInfo.Fields.TEMPLATE_NAME, terraformTemplate.getName()));
    }

    private void validateTemplateStatus(final TerraformTemplate terraformTemplate) {
        final TemplateStatus templateStatus = terraformTemplate.getStatus();
        if (!templateStatus.isPaasRegistrationAllowed()) {
            throw new RuntimeException(String.format("Terraform template '%s' has invalid status: '%s'",
                    terraformTemplate.getName(), templateStatus));
        }
    }

    @Override
    public String buildRegistrationMessage(final PlatformServiceDefinition platformServiceDefinition) {
        return String.format("Platform service %s successfully registered with base template: %s",
                platformServiceDefinition.getName(), platformServiceDefinition.getServiceDeploymentInfo().getProviderParams().get(PlatformServiceDeploymentInfo.Fields.TEMPLATE_NAME));
    }

    @Override
    public PlatformServiceEntry handleServiceActivation(final ITenant tenant, final IRegion region,
                                                        final Map<String, String> providerParams,
                                                        final Map<String, TerraformUserVariable> userVariables,
                                                        final String requester, final String serviceName) {
        final String systemPaasTemplateName = providerParams.get(PlatformServiceDeploymentInfo.Fields.TEMPLATE_NAME);
        final TerraformTemplate paasTemplate = terraformTemplateService.findSystemTemplateByName(systemPaasTemplateName)
                .orElseThrow(() -> new RuntimeException("PaaS template not found " + systemPaasTemplateName));

        final PlatformServiceEntry serviceEntry = new PlatformServiceEntry()
                .withServiceName(serviceName)
                .withDeploymentInfo(new PlatformServiceDeploymentInfo()
                        .withProviderType(PlatformServiceDeploymentType.TERRAFORM)
                        .withProviderParams(providerParams))
                .withCloud(tenant.getCloud().name())
                .withTenantName(tenant.getName())
                .withTenantDisplayName(tenant.getTenantAlias())
                .withRegionName(region.getRegionAlias())
                .withOwner(requester)
                .withState(PlatformServiceEntryState.UNKNOWN);

        final String taskId = entryService.addTaskInProgress(serviceEntry, PlatformServiceTask.ACTIVATION);
        final IContextExtension contextExtension = new PaasTerraformPipeLineContextExtension(serviceName, serviceEntry.getServiceEntryId(), taskId, region.getRegionAlias());
        try {
            TenantInfo tenantInfo = new TenantInfo(tenant.getName(), tenant.getTenantAlias(), tenant.getCloud().name());
            paasStorageManager.applyPaasTemplate(tenantInfo, new TerraformTemplateInfoWrapper(paasTemplate), requester, userVariables, contextExtension);
        } catch (Exception e) {
            entryService.removeTask(serviceEntry);
            throw e;
        }
        return serviceEntry;
    }

    @Override
    public PlatformServiceEntryInfo convertEntryInfo(final PlatformServiceEntry platformServiceEntry) {
        final Map<String, String> providerParams = platformServiceEntry.getDeploymentInfo().getProviderParams();
        final String templateName = providerParams.get(PlatformServiceDeploymentInfo.Fields.TEMPLATE_NAME);
        return new PlatformServiceEntryInfo(platformServiceEntry.getServiceName(), platformServiceEntry.getServiceEntryId(),
                platformServiceEntry.getState(), templateName, platformServiceEntry.getServiceUrl());
    }

    @Override
    public Collection<PlatformServiceVariable> getDeclaredServiceVariables(final Map<String, String> providerParams) {
        final String systemServiceTemplateName = providerParams.get(PlatformServiceDeploymentInfo.Fields.TEMPLATE_NAME);
        final TerraformTemplate paasTemplate = terraformTemplateService.findSystemTemplateByName(systemServiceTemplateName)
                .orElseThrow(() -> new RuntimeException("Platform service terraform template not found " + systemServiceTemplateName));
        return gatherServiceVariables(paasTemplate);
    }

    private Collection<PlatformServiceVariable> gatherServiceVariables(final TerraformTemplate paasTemplate) {
        final Map<String, TerraformTemplateVariable> templateVariables = paasTemplate.getTemplateVariables();
        if (CollectionUtils.isEmpty(templateVariables)) {
            return Collections.emptyList();
        }
        final Map<String, TerraformUserVariable> predefinedVariables = paasTemplate.getVariables();
        final TerraformTemplateInfoWrapper templateInfoWrapper = new TerraformTemplateInfoWrapper(paasTemplate);
        final Map<String, Object> templateSecrets = sensitiveVariableManager.getTemplateSecrets(templateInfoWrapper);
        final Map<String, Object> predefinedSecrets = sensitiveVariableManager.getPredefinedSecrets(templateInfoWrapper);

        final Collection<PlatformServiceVariable> result = new ArrayList<>(templateVariables.size());
        for (TerraformTemplateVariable templateVariable : templateVariables.values()) {
            final String variableName = templateVariable.getName();
            final Object defaultValue = templateVariable.isSecured() ? templateSecrets.get(variableName) : templateVariable.getDefaultValue();
            final Object value = Optional.ofNullable(predefinedVariables.get(variableName))
                    .map(predefinedVariable -> predefinedVariable.isSecured() ? predefinedSecrets.get(variableName) : predefinedVariable.getValue())
                    .orElse(null);
            final PlatformServiceVariable platformServiceVariable = new PlatformServiceVariable(variableName,
                    TerraformVariableUtils.convertVariableType(templateVariable.getType()), defaultValue, value,
                    templateVariable.getDescription(), templateVariable.isSensitive());
            result.add(platformServiceVariable);
        }
        return result;
    }

    @Override
    public Collection<PlatformServiceEntryInfo> getServiceEntryInfos(final Collection<PlatformServiceEntry> serviceEntries) {
        return serviceEntries.stream()
                .map(this::buildServiceEntryInfo)
                .collect(Collectors.toList());
    }

    private PlatformServiceEntryInfo buildServiceEntryInfo(final PlatformServiceEntry serviceEntry) {
        Map<String, String> providerParams = serviceEntry.getDeploymentInfo().getProviderParams();
        final String templateName = providerParams.get(PlatformServiceDeploymentInfo.Fields.TEMPLATE_NAME);
        return new PlatformServiceEntryInfo(serviceEntry.getServiceName(), serviceEntry.getServiceEntryId(),
                serviceEntry.getState(), templateName, serviceEntry.getServiceUrl());
    }

    @Override
    public Collection<PlatformServiceDefinitionInfo> getServiceDefinitionInfos(final Collection<PlatformServiceDefinition> serviceDefinitions) {
        return serviceDefinitions.stream()
                .map(definition -> new PlatformServiceDefinitionInfo(definition.getName(), definition.getProductVersion(),
                        definition.getSupportedClouds(), definition.getCategories(), null, PlatformServiceDeploymentType.TERRAFORM.name()))
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateService(final PlatformServiceEntry serviceEntry, final String requester) {
        assertActionAllowed(serviceEntry);
        final Map<String, String> providerParams = serviceEntry.getDeploymentInfo().getProviderParams();
        final String systemServiceTemplateName = providerParams.get(PlatformServiceDeploymentInfo.Fields.TEMPLATE_NAME);
        final TerraformTemplate serviceTemplate = terraformTemplateService.findSystemTemplateByName(systemServiceTemplateName)
                .orElseThrow(() -> new RuntimeException("Platform service terraform template not found " + systemServiceTemplateName));
        final TerraformStack serviceEntryStack = terraformStackService.findByServiceEntry(serviceEntry)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Terraform stack for platform service entry %s not found", serviceEntry.getServiceEntryId())));
        IRegion region = regionRepository.findByAliasInCloud(serviceEntry.getRegionName(), serviceEntry.getCloud());
        final ITenant tenant = tenantService.findByTenantAliasAndRegionId(serviceEntry.getTenantDisplayName(), region.getId());

        final String taskId = entryService.addTaskInProgress(serviceEntry, PlatformServiceTask.DEACTIVATION);
        final IContextExtension contextExtension = new PaasTerraformPipeLineContextExtension(
                serviceEntry.getServiceName(), serviceEntryStack.getServiceEntryId(), taskId, serviceEntry.getRegionName());
        try {
            TenantInfo tenantInfo = new TenantInfo(tenant.getName(), tenant.getTenantAlias(), tenant.getCloud().name());
            paasStorageManager.destroyPaasTemplateStack(tenantInfo, new TerraformTemplateInfoWrapper(serviceTemplate),
                    new TerraformStackInfoWrapper(serviceEntryStack), requester, contextExtension);
        } catch (Exception e) {
            entryService.removeTask(serviceEntry);
            throw e;
        }
    }

    @Override
    public void unregisterService(final PlatformServiceDefinition serviceDefinition,
                                  final boolean serviceEntryExists) {
        final String serviceName = serviceDefinition.getName();
        final Map<String, String> providerParams = serviceDefinition.getServiceDeploymentInfo().getProviderParams();
        final String systemServiceTemplateName = providerParams.get(PlatformServiceDeploymentInfo.Fields.TEMPLATE_NAME);
        final Optional<TerraformTemplate> serviceTemplate = terraformTemplateService.findSystemTemplateByName(systemServiceTemplateName);
        if (serviceTemplate.isEmpty()) {
            LOG.warn("System terraform template {} for platform service {} not found", systemServiceTemplateName, serviceName);
            return;
        }
        final TerraformTemplateInfoWrapper templateInfo = new TerraformTemplateInfoWrapper(serviceTemplate.get());
        paasStorageManager.deletePaasTemplateBase(templateInfo, serviceName);
        LOG.info("Base {} service template was deleted from internal storage", serviceName);
        sensitiveVariableManager.deleteSecrets(templateInfo);
        LOG.info("Base {} service template's secrets were deleted", serviceName);
        if (!serviceEntryExists) {
            terraformTemplateService.delete(serviceTemplate.get().getTemplateId());
            LOG.info("System template for {} service was deleted", serviceName);
        } else {
            LOG.info("There are {} service activations, system template will be deleted after their deactivation", serviceName);
        }
    }

    @Override
    public PlatformServiceDeploymentType getDeploymentType() {
        return PlatformServiceDeploymentType.TERRAFORM;
    }

    @Override
    public void processTerraformPipeLineResult(final PlatformServiceEntry serviceEntry,
                                               final PaasTerraformPipeLineContextExtension contextExtension,
                                               final ITerraformPipeLineResultObserver.PipeLineStatus status) {
        final Map<String, String> providerParams = serviceEntry.getDeploymentInfo().getProviderParams();
        final String taskId = providerParams.get(PlatformServiceDeploymentInfo.Fields.TASK_ID);
        if (!Objects.equals(taskId, contextExtension.getTaskId())) {
            LOG.error("Got execution result for unregistered task: {}", contextExtension.getTaskId());
            return;
        }

        final String taskName = providerParams.get(PlatformServiceDeploymentInfo.Fields.TASK_NAME);
        final PlatformServiceTask serviceTask = PlatformServiceTask.valueOf(taskName);
        LOG.info("Got execution result for task {ID: {}, name: {}} for service {}: {}", taskId, taskName, serviceEntry.getServiceEntryId(), status);
        try {
            if (serviceTask == PlatformServiceTask.ACTIVATION) {
                processServiceActivationCompletion(serviceEntry, status);
            } else if (serviceTask == PlatformServiceTask.DEACTIVATION) {
                processServiceDeactivationCompletion(serviceEntry, status);
            } else {
                LOG.warn("No handling for {} service task found", serviceTask);
            }
        } finally {
            entryService.removeTask(serviceEntry);
        }
    }

    private void processServiceActivationCompletion(final PlatformServiceEntry serviceEntry,
                                                    final ITerraformPipeLineResultObserver.PipeLineStatus status) {
        PlatformServiceEntryState entryState;
        switch (status) {
            case SUCCESS:
                entryState = PlatformServiceEntryState.INITIALIZED;
                break;
            case FAILURE:
            case ERROR:
                entryState = PlatformServiceEntryState.ERROR;
                break;
            default:
                throw new IllegalArgumentException();
        }
        entryService.updateServiceEntryState(serviceEntry, entryState);
    }

    private void processServiceDeactivationCompletion(final PlatformServiceEntry serviceEntry,
                                                      final ITerraformPipeLineResultObserver.PipeLineStatus status) {
        if (status != ITerraformPipeLineResultObserver.PipeLineStatus.SUCCESS) {
            entryService.updateServiceEntryState(serviceEntry, PlatformServiceEntryState.ERROR);
            return;
        }

        entryService.delete(serviceEntry);

        final String serviceName = serviceEntry.getServiceName();
        final boolean serviceExists = definitionService.isServiceExist(serviceName);
        if (serviceExists) {
            return;
        }
        final boolean serviceActivationExists = entryService.isServiceActivationExist(serviceName);
        if (serviceActivationExists) {
            return;
        }

        final Map<String, String> providerParams = serviceEntry.getDeploymentInfo().getProviderParams();
        final String systemServiceTemplateName = providerParams.get(PlatformServiceDeploymentInfo.Fields.TEMPLATE_NAME);
        terraformTemplateService.findSystemTemplateByName(systemServiceTemplateName)
                .map(TerraformTemplate::getTemplateId)
                .ifPresent(systemTemplateId -> {
                    terraformTemplateService.delete(systemTemplateId);
                    LOG.info("System template for {} service was deleted", serviceName);
                });
    }

    private void assertActionAllowed(final PlatformServiceEntry entry) {
        final Map<String, String> providerParams = entry.getDeploymentInfo().getProviderParams();
        final String currentTask = providerParams.get(PlatformServiceDeploymentInfo.Fields.TASK_NAME);
        if (StringUtils.isNotBlank(currentTask)) {
            throw new RuntimeException(String.format("Platform service entry %s is under %s task. Please wait for the current task to complete",
                    entry.getServiceEntryId(), currentTask.toLowerCase()));
        }
    }
}
