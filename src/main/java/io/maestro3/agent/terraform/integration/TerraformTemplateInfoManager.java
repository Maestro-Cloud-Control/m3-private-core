package io.maestro3.agent.terraform.integration;

import io.maestro3.agent.terraform.integration.context.PaasTerraformPipeLineContextExtension;
import io.maestro3.agent.terraform.sevice.ITerraformTemplateService;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.model.TerraformTemplateDbUpdateParameters;
import io.maestro3.agent.terraform.model.TerraformTemplateInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.management.interfaces.model.ITerraformEngineTemplateInfo;
import team.syndicate.terraform.engine.management.interfaces.service.ITerraformEngineTemplateInfoService;
import team.syndicate.terraform.engine.management.model.TerraformEngineTemplateUpdateContext;
import team.syndicate.terraform.engine.management.model.TerraformTemplateCreationData;
import team.syndicate.terraform.engine.terraform.integration.IContextExtension;
import team.syndicate.terraform.engine.terraform.integration.model.ITerraformTemplateInfo;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateUpdateContext;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class TerraformTemplateInfoManager implements ITerraformEngineTemplateInfoService {

    private final ITerraformTemplateService templateService;

    @Autowired
    public TerraformTemplateInfoManager(ITerraformTemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public Optional<ITerraformTemplateInfo> getTemplateInfo(String tenantName, String templateName, IContextExtension contextExtension) {
        Optional<TerraformTemplate> terraformTemplate;
        if (contextExtension instanceof PaasTerraformPipeLineContextExtension) {
            terraformTemplate = templateService.findSystemTemplateByName(templateName);
        } else {
            terraformTemplate = templateService.find(tenantName, templateName);
        }
        return terraformTemplate.map(TerraformTemplateInfoWrapper::new);
    }

    @Override
    public void updateTemplate(final ITerraformTemplateInfo template,
                               final TerraformTemplateUpdateContext updateContext) {
        if (template == null) {
            return;
        }

        final TerraformTemplateDbUpdateParameters updateParameters = new TerraformTemplateDbUpdateParameters();
        addUpdateClause(updateContext, TerraformTemplateUpdateContext::getNewStatus,
                newTemplateStatus -> newTemplateStatus != template.getStatus(),
                updateParameters::withNewTemplateStatus);
        addUpdateClause(updateContext, TerraformTemplateUpdateContext::getNewProviders,
                newProviders -> !Objects.equals(newProviders, template.getProviders()),
                updateParameters::withNewProviders);
        addUpdateClause(updateContext, TerraformTemplateUpdateContext::getNewTemplateVariables,
                newTemplateVariables -> true,
                updateParameters::withNewTemplateVariables);
        addUpdateClause(updateContext, TerraformTemplateUpdateContext::getNewUserVariables,
                newUserVariables -> true,
                updateParameters::withNewUserVariables);

        if (updateContext instanceof TerraformEngineTemplateUpdateContext
                && template instanceof ITerraformEngineTemplateInfo) {
            TerraformEngineTemplateUpdateContext engineTemplateUpdateContext = (TerraformEngineTemplateUpdateContext) updateContext;
            ITerraformEngineTemplateInfo engineTemplate = (ITerraformEngineTemplateInfo) template;
            addUpdateClause(engineTemplateUpdateContext, TerraformEngineTemplateUpdateContext::getNewTask,
                    newTask -> !engineTemplate.getTasksInProgress().containsKey(newTask.getTaskId()),
                    updateParameters::withNewTask);
            addUpdateClause(engineTemplateUpdateContext, TerraformEngineTemplateUpdateContext::getNewAutoTask,
                    newAutoTask -> engineTemplate.getAutoTaskQueue().contains(newAutoTask),
                    updateParameters::withNewAutoTask);
            addUpdateClause(engineTemplateUpdateContext, TerraformEngineTemplateUpdateContext::getNewStorageInfo,
                    newStorageInfo -> true,
                    updateParameters::withNewStorageInfo);
        }

        templateService.updateTemplate(template.getTemplateId(), updateParameters);
    }

    private <T, C extends TerraformTemplateUpdateContext> void addUpdateClause(final C updateContext,
                                                                               final Function<C, TerraformTemplateUpdateContext.UpdateParameter<T>> extractUpdateParamFunction,
                                                                               final Predicate<T> acceptanceCondition,
                                                                               final Consumer<T> consumer) {
        final TerraformTemplateUpdateContext.UpdateParameter<T> updateParameter = extractUpdateParamFunction.apply(updateContext);
        Optional.ofNullable(updateParameter)
                .map(TerraformTemplateUpdateContext.UpdateParameter::getValue)
                .filter(acceptanceCondition)
                .ifPresent(consumer);
    }

    @Override
    public ITerraformEngineTemplateInfo create(final TerraformTemplateCreationData data) {
        final TerraformTemplate template = new TerraformTemplate()
                .withName(data.getTemplateName())
                .withDescription(data.getDescription())
                .withTenantName(data.getTenantName())
                .withTenantDisplayName(data.getTenantDisplayName())
                .withCloud(data.getCloud())
                .withStatus(data.getStatus())
                .withFormat(data.getFormat())
                .withOwner(data.getOwner())
                .withMultiStack(data.isMultiStack());
        templateService.save(template);
        return new TerraformTemplateInfoWrapper(template);
    }

    @Override
    public Optional<ITerraformEngineTemplateInfo> find(final String templateId) {
        return templateService.find(templateId)
                .map(TerraformTemplateInfoWrapper::new);
    }

    @Override
    public void removeTask(final String templateId, final String taskId) {
        templateService.removeTask(templateId, taskId);
    }

    @Override
    public Collection<ITerraformEngineTemplateInfo> findTemplatesWithQueuedTasks() {
        return templateService.findTemplatesWithQueuedTasks().stream()
                .map(TerraformTemplateInfoWrapper::new)
                .map(ITerraformEngineTemplateInfo.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public String pollQueuedTask(final ITerraformEngineTemplateInfo template) {
        templateService.pollQueuedTask(template.getTemplateId());
        return template.getAutoTaskQueue().poll();
    }

    @Override
    public void delete(final String templateId) {
        templateService.delete(templateId);
    }
}
