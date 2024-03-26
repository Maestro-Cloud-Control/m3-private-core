package io.maestro3.agent.terraform.storage.impl.manager;

import io.maestro3.agent.model.base.BaseTenant;
import io.maestro3.agent.terraform.exception.BadRequestException;
import io.maestro3.agent.terraform.exception.ResourceNotFoundException;
import io.maestro3.agent.terraform.manager.ITerraformStackService;
import io.maestro3.agent.terraform.manager.ITerraformTemplateService;
import io.maestro3.agent.terraform.model.CreateOrUpdateTerraformTemplateParams;
import io.maestro3.agent.terraform.model.DeleteTerraformTemplateResult;
import io.maestro3.agent.terraform.model.TerraformStack;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.sender.IRabbitMqTerraformMessageSender;
import io.maestro3.agent.terraform.storage.ITerraformTemplateStorageManager;
import io.maestro3.agent.terraform.storage.internal.ITerraformTemplateInternalStorageCaller;
import io.maestro3.agent.terraform.util.TerraformTemplateTaskValidator;
import io.maestro3.agent.tf.integration.TerraformPipeLineContext;
import io.maestro3.agent.tf.integration.model.ITerraformStackInfo;
import io.maestro3.agent.tf.integration.model.TemplateFormat;
import io.maestro3.agent.tf.integration.model.TemplateStatus;
import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageInfo;
import io.maestro3.agent.tf.integration.model.TerraformVariable;
import io.maestro3.agent.tf.integration.task.ExecuteTerraformTaskRequest;
import io.maestro3.agent.tf.integration.task.TerraformTask;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.slf4j.Logger;
import team.syndicate.terraform.integration.TerraformPipeLineContext;
import team.syndicate.terraform.integration.model.ITerraformStackInfo;
import team.syndicate.terraform.integration.model.TemplateFormat;
import team.syndicate.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.integration.model.TerraformTemplateStorageInfo;
import team.syndicate.terraform.integration.model.TerraformTemplateVariable;
import team.syndicate.terraform.integration.task.ExecuteTerraformTaskRequest;
import team.syndicate.terraform.integration.task.TerraformTask;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractTerraformTemplateManager implements ITerraformTemplateStorageManager {

    protected static final String CHANGE_STORAGE_TYPE_ERROR_MESSAGE = "Template %s already added with storage type %s. " +
            "The change of storage type is prohibited";
    private static final Pattern TEMPLATE_NAME_PATTERN = Pattern.compile("^[\\w+!\\-_.*'()]{2,128}$");
    private static final String INVALID_TEMPLATE_NAME_ERROR_MESSAGE = "The length of name should be between 2 and 128 symbols. " +
            "The following characters are safe for use in template names: " +
            "0-9, a-z, A-Z, !, -, _, ., *, ', (, )";

    protected final ITerraformTemplateInternalStorageCaller internalStorageCaller;
    private final ITerraformTemplateService terraformTemplateService;
    private final ITerraformStackService stackService;
    private final IRabbitMqTerraformMessageSender terraformMessageSender;

    protected AbstractTerraformTemplateManager(ITerraformTemplateService terraformTemplateService,
                                               ITerraformTemplateInternalStorageCaller internalStorageCaller,
                                               ITerraformStackService stackService,
                                               IRabbitMqTerraformMessageSender terraformMessageSender) {
        this.terraformTemplateService = terraformTemplateService;
        this.internalStorageCaller = internalStorageCaller;
        this.stackService = stackService;
        this.terraformMessageSender = terraformMessageSender;
    }

    @Override
    public TerraformTemplate createTemplate(final BaseTenant tenant, final String owner,
                                            final CreateOrUpdateTerraformTemplateParams params) {
        final String tenantName = tenant.getId().toUpperCase();
        final String templateName = params.getTemplateName();
        validateTemplateName(templateName);

        final TerraformTemplate terraformTemplate = new TerraformTemplate()
                .withName(templateName)
                .withDescription(params.getDescription())
                .withTenantName(tenantName)
                .withTenantDisplayName(tenant.getTenantAlias())
                .withCloud(tenant.getCloud().name())
                .withStatus(TemplateStatus.PENDING_VALIDATION)
                .withFormat(TemplateFormat.HCL) // todo: support json templates
                .withOwner(owner)
                .withMultiStack(params.isMultiStack())
                .withVariables(params.getVariables());
        final TerraformTemplateStorageInfo storageInfo = configureStorage(terraformTemplate, params);
        terraformTemplate.withStorageInfo(storageInfo);
        terraformTemplateService.save(terraformTemplate);
        getLogger().info("Template {} info for tenant {} was added", templateName, tenantName);

        ExecuteTerraformTaskRequest terraformTaskRequest = buildTaskRequest(terraformTemplate, owner, TerraformTask.VALIDATE);
        terraformTemplateService.addTaskInProgress(terraformTemplate, terraformTaskRequest.getId(), TerraformTask.VALIDATE);
        terraformMessageSender.send(terraformTaskRequest);
        getLogger().info("Terraform task for template {} validation was submitted", templateName);

        return terraformTemplate;
    }

    private void validateTemplateName(final String providedName) {
        final Matcher matcher = TEMPLATE_NAME_PATTERN.matcher(providedName);
        if (!matcher.matches()) {
            throw new BadRequestException(INVALID_TEMPLATE_NAME_ERROR_MESSAGE);
        }
    }

    protected abstract TerraformTemplateStorageInfo configureStorage(final TerraformTemplate terraformTemplate,
                                                                     final CreateOrUpdateTerraformTemplateParams params);

    @Override
    public TerraformTemplate updateTemplate(final TerraformTemplate existingTemplate,
                                            final String requester,
                                            final CreateOrUpdateTerraformTemplateParams params) {
        TerraformTemplateTaskValidator.assertTaskAllowed(existingTemplate, TerraformTask.VALIDATE);
        updateStorageContent(existingTemplate, params);

        existingTemplate.withStatus(TemplateStatus.PENDING_VALIDATION);
        terraformTemplateService.save(existingTemplate);

        ExecuteTerraformTaskRequest terraformTaskRequest = buildTaskRequest(existingTemplate, requester, TerraformTask.VALIDATE);
        terraformTemplateService.addTaskInProgress(existingTemplate, terraformTaskRequest.getId(), TerraformTask.VALIDATE);
        terraformMessageSender.send(terraformTaskRequest);
        getLogger().info("Terraform task for template {} validation was submitted", existingTemplate.getName());

        return existingTemplate;
    }

    protected abstract void updateStorageContent(final TerraformTemplate existingTemplate,
                                                 final CreateOrUpdateTerraformTemplateParams params);

    @Override
    public void planTemplate(final TerraformTemplate template, final String requester,
                             final Map<String, TerraformTemplateVariable> requestVariables) {
        planTemplate(template, requester, requestVariables, false);
    }

    protected void planTemplate(final TerraformTemplate template, final String requester,
                                final Map<String, TerraformTemplateVariable> requestVariables, final boolean autoTask) {
        TerraformTemplateTaskValidator.assertTaskAllowed(template, TerraformTask.PLAN, autoTask);
        ExecuteTerraformTaskRequest terraformTaskRequest = buildTaskRequest(template, null,
                requester, TerraformTask.PLAN, requestVariables, null);
        terraformTemplateService.updateTemplateStatus(template, TemplateStatus.PENDING_PLANNING);
        terraformTemplateService.addTaskInProgress(template, terraformTaskRequest.getId(), TerraformTask.PLAN);
        terraformMessageSender.send(terraformTaskRequest);
        getLogger().info("Terraform task for template {} planning was submitted", template.getName());
    }

    @Override
    public void applyTemplate(final TerraformTemplate template, final String requester,
                              final Map<String, TerraformTemplateVariable> requestVariables,
                              final String paasUuid) {
        applyTemplate(template, requester, requestVariables, false, paasUuid);
    }

    protected void applyTemplate(final TerraformTemplate template, final String requester,
                                 final Map<String, TerraformTemplateVariable> requestVariables,
                                 boolean autoTask,
                                 final String paasUuid) {
        TerraformTemplateTaskValidator.assertTaskAllowed(template, TerraformTask.APPLY, autoTask);
        final ExecuteTerraformTaskRequest terraformTaskRequest = buildTaskRequest(template, null, requester,
                TerraformTask.APPLY, requestVariables, paasUuid);
        if (!template.isMultiStack()) {
            terraformTemplateService.updateTemplateStatus(template, TemplateStatus.PENDING_PLANNING);
            stackService.getSingleStack(template.getTemplateId())
                    .ifPresent(terraformStack -> stackService.updateStackStatus(terraformStack, TemplateStatus.PENDING_PLANNING));
        }
        terraformTemplateService.addTaskInProgress(template, terraformTaskRequest.getId(), TerraformTask.APPLY);
        terraformMessageSender.send(terraformTaskRequest);
        getLogger().info("Terraform task for template {} applying was submitted", template.getName());
    }

    @Override
    public void destroyTemplateStack(final TerraformTemplate template, final String stackId, final String requester) {
        final TerraformStack stack = resolveStack(stackId, template.getTenantName(), template.getName());
        TerraformTemplateTaskValidator.assertTaskAllowed(template, stack, TerraformTask.DESTROY);
        ExecuteTerraformTaskRequest terraformTaskRequest = buildTaskRequest(template, stack,
                requester, TerraformTask.DESTROY, null, null);
        stackService.updateStackStatus(stack, TemplateStatus.PENDING_DESTROY);
        if (!template.isMultiStack()) {
            terraformTemplateService.updateTemplateStatus(template, TemplateStatus.PENDING_DESTROY);
            terraformTemplateService.addTaskInProgress(template, terraformTaskRequest.getId(), TerraformTask.DESTROY);
        }
        terraformMessageSender.send(terraformTaskRequest);
        getLogger().info("Terraform task for stack {} destroying was submitted", stack.getStackId());
    }

    private TerraformStack resolveStack(final String stackId, final String tenantName,
                                        final String templateName) {
        if (StringUtils.isNotBlank(stackId)) {
            return stackService.findByTemplateName(tenantName, templateName, stackId)
                    .orElseThrow(() -> new ResourceNotFoundException("Terraform stack", stackId));
        }

        Collection<TerraformStack> templateStacks = stackService.findByTemplateName(tenantName, templateName);
        if (CollectionUtils.isEmpty(templateStacks)) {
            throw new BadRequestException(String.format("Template %s has no stacks to destroy", templateName));
        }
        if (templateStacks.size() > 1) {
            throw new BadRequestException(String.format("Template %s has multiple stacks, specify stackId to destroy", templateName));
        }

        return templateStacks.iterator().next();
    }

    private ExecuteTerraformTaskRequest buildTaskRequest(final TerraformTemplate template,
                                                         final String requester,
                                                         final TerraformTask task) {
        return buildTaskRequest(template, null, requester, task, null, null);
    }

    private ExecuteTerraformTaskRequest buildTaskRequest(final TerraformTemplate template,
                                                         final TerraformStack stack,
                                                         final String requester,
                                                         final TerraformTask task,
                                                         final Map<String, TerraformTemplateVariable> variables,
                                                         String paasUuid) {
        return new ExecuteTerraformTaskRequest()
                .withCloud(template.getCloud())
                .withTenantName(template.getTenantName())
                .withPaasUuid(paasUuid)
                .withTenantDisplayName(template.getTenantDisplayName())
                .withTemplateName(template.getName())
                .withTemplateId(template.getTemplateId())
                .withStackId(Optional.ofNullable(stack).map(TerraformStack::getStackId).orElse(null))
                .withRequester(requester)
                .withTask(task)
                .withVariables(variables);
    }

    @Override
    public DeleteTerraformTemplateResult deleteTemplate(final TerraformTemplate template) {
        String templateName = template.getName();
        String tenantName = template.getTenantName();

        Collection<TerraformStack> stacks = stackService.findByTemplateName(tenantName, templateName);
        if (CollectionUtils.isNotEmpty(stacks)) {
            return new DeleteTerraformTemplateResult(false,
                    String.format("Terraform template '%s' can't be removed. There are some stacks that were applied from this template", templateName));
        }

        boolean tasksInProgress = CollectionUtils.isNotEmpty(template.getTasksInProgress());
        if (tasksInProgress) {
            return new DeleteTerraformTemplateResult(false,
                    String.format("Terraform template '%s' is processing now, deletion is prohibited", templateName));
        }

        clearStorageContent(template);
        terraformTemplateService.delete(template.getTemplateId());
        return new DeleteTerraformTemplateResult(true, String.format("Terraform template '%s' was deleted successfully", templateName));
    }

    protected abstract void clearStorageContent(final TerraformTemplate template);

    @Override
    public File downloadTemplate(final TerraformPipeLineContext context, final File targetDirectory) {
        ITerraformStackInfo stack = context.getStack();
        if (stack != null && !context.isFreshTemplateCopyRequired()) {
            internalStorageCaller.downloadStackTemplateCopy(context, targetDirectory);
            return getTemplateFile(targetDirectory, stack.getSubDirectory());
        }

        return downloadTemplateFromStorage(context, targetDirectory);
    }

    protected File getTemplateFile(File targetDirectory, String subDirectory) {
        if (StringUtils.isBlank(subDirectory)) {
            return targetDirectory;
        }

        Path templateDirectoryPath = Paths.get(targetDirectory.getAbsolutePath(), subDirectory);
        return templateDirectoryPath.toFile();
    }

    protected abstract File downloadTemplateFromStorage(final TerraformPipeLineContext context, final File targetDirectory);

    protected abstract Logger getLogger();
}
