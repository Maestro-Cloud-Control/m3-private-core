package io.maestro3.agent.terraform.storage.impl.manager;

import io.maestro3.agent.terraform.api.IApiUrlProvider;
import io.maestro3.agent.terraform.manager.ITerraformStackService;
import io.maestro3.agent.terraform.manager.ITerraformTemplateService;
import io.maestro3.agent.terraform.model.CreateOrUpdateTerraformTemplateParams;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.sender.IRabbitMqTerraformMessageSender;
import io.maestro3.agent.terraform.storage.internal.ITerraformTemplateInternalStorageCaller;
import io.maestro3.agent.terraform.webhook.SupportsTerraformWebhook;
import io.maestro3.agent.terraform.webhook.processor.ITerraformWebhookProcessor;
import io.maestro3.agent.terraform.webhook.processor.ITerraformWebhookProcessorProvider;
import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageInfo;
import io.maestro3.sdk.v3.model.terraform.WebHookAction;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractTerraformWebhookTemplateManager extends AbstractTerraformTemplateManager implements SupportsTerraformWebhook {

    private final ITerraformWebhookProcessorProvider webhookProcessorProvider;
    protected final IApiUrlProvider apiUrlProvider;

    protected AbstractTerraformWebhookTemplateManager(ITerraformTemplateService terraformTemplateService,
                                                      ITerraformTemplateInternalStorageCaller internalStorageCaller,
                                                      ITerraformStackService stackService,
                                                      IRabbitMqTerraformMessageSender terraformMessageSender,
                                                      ITerraformWebhookProcessorProvider webhookProcessorProvider,
                                                      IApiUrlProvider apiUrlProvider) {
        super(terraformTemplateService, internalStorageCaller, stackService, terraformMessageSender);
        this.webhookProcessorProvider = webhookProcessorProvider;
        this.apiUrlProvider = apiUrlProvider;
    }

    @Override
    protected TerraformTemplateStorageInfo configureStorage(final TerraformTemplate terraformTemplate,
                                                            final CreateOrUpdateTerraformTemplateParams params) {
        final TerraformTemplateStorageInfo storageInfo = validateStorageInfo(terraformTemplate, params);
        saveStorageCredentials(terraformTemplate, params);
        final Long webhookId = createWebhook(terraformTemplate, params);
        Optional.ofNullable(webhookId)
                .ifPresent(id -> storageInfo.getStorageParams().put(TerraformTemplateStorageInfo.Fields.WEBHOOK_ID, String.valueOf(id)));
        return storageInfo;
    }

    @Override
    public void updateStorageContent(final TerraformTemplate existingTemplate,
                                     final CreateOrUpdateTerraformTemplateParams params) {
        verifyUpdatePossible(existingTemplate, params);
        final TerraformTemplateStorageInfo newStorageInfo = validateStorageInfo(existingTemplate, params);

        boolean needToDeleteOldWebhook = needToDeleteOldWebhook(existingTemplate, newStorageInfo);
        if (needToDeleteOldWebhook) {
            deleteWebhook(existingTemplate);
        }

        saveStorageCredentials(existingTemplate, params);

        boolean needToUpdateExistingWebhook = !needToDeleteOldWebhook &&
                needToUpdateExistingWebhook(existingTemplate.getStorageInfo(), newStorageInfo);
        if (needToUpdateExistingWebhook) {
            updateExistingWebhook(existingTemplate, newStorageInfo);
        } else {
            final Long newWebhookId = createWebhook(existingTemplate, params);
            Optional.ofNullable(newWebhookId)
                    .ifPresent(id -> newStorageInfo.getStorageParams().put(TerraformTemplateStorageInfo.Fields.WEBHOOK_ID, String.valueOf(id)));
        }

        existingTemplate.withStorageInfo(newStorageInfo);
    }

    private boolean needToDeleteOldWebhook(final TerraformTemplate existingTemplate,
                                           final TerraformTemplateStorageInfo newStorageInfo) {
        final TerraformTemplateStorageInfo oldStorageInfo = existingTemplate.getStorageInfo();
        final String oldWebhookActionString = oldStorageInfo.getStorageParams().get(TerraformTemplateStorageInfo.Fields.WEBHOOK_ACTION);
        final WebHookAction oldWebhookAction = WebHookAction.fromValue(oldWebhookActionString);
        final String newWebhookActionString = newStorageInfo.getStorageParams().get(TerraformTemplateStorageInfo.Fields.WEBHOOK_ACTION);
        final WebHookAction newWebhookAction = WebHookAction.fromValue(newWebhookActionString);
        final ITerraformWebhookProcessor newWebhookProcessor = webhookProcessorProvider.provide(newWebhookAction);
        boolean needToDeleteOldWebhook = newWebhookProcessor.needToDeleteOldWebhook(oldWebhookAction);
        return needToDeleteOldWebhook || needToDeleteOldWebhook(oldStorageInfo, newStorageInfo);
    }

    protected abstract boolean needToDeleteOldWebhook(final TerraformTemplateStorageInfo oldStorageInfo,
                                                      final TerraformTemplateStorageInfo newStorageInfo);

    protected abstract boolean needToUpdateExistingWebhook(final TerraformTemplateStorageInfo oldStorageInfo,
                                                           final TerraformTemplateStorageInfo newStorageInfo);

    protected abstract void updateExistingWebhook(final TerraformTemplate existingTemplate,
                                                  final TerraformTemplateStorageInfo newStorageInfo);

    private CreateOrUpdateTerraformTemplateParams.SupportWebhookStorageInfo getWebhookStorageInfo(final CreateOrUpdateTerraformTemplateParams params) {
        final CreateOrUpdateTerraformTemplateParams.StorageSpecificInfo storageSpecificInfo = params.getStorageInfo();
        if (storageSpecificInfo instanceof CreateOrUpdateTerraformTemplateParams.SupportWebhookStorageInfo) {
            return (CreateOrUpdateTerraformTemplateParams.SupportWebhookStorageInfo) storageSpecificInfo;
        }
        throw new IllegalArgumentException("Got malformed terraform template params");
    }

    private Long createWebhook(final TerraformTemplate terraformTemplate, final CreateOrUpdateTerraformTemplateParams params) {
        final CreateOrUpdateTerraformTemplateParams.SupportWebhookStorageInfo webhookStorageInfo = getWebhookStorageInfo(params);
        final String webhookActionString = webhookStorageInfo.getWebHookAction();
        final WebHookAction webHookAction = WebHookAction.fromValue(webhookActionString);
        final ITerraformWebhookProcessor webhookProcessor = webhookProcessorProvider.provide(webHookAction);
        getLogger().info("Setting up new webhook");
        return webhookProcessor.setupWebhook(this, terraformTemplate, params);
    }

    protected abstract TerraformTemplateStorageInfo validateStorageInfo(final TerraformTemplate terraformTemplate,
                                                                        final CreateOrUpdateTerraformTemplateParams params);

    protected abstract void saveStorageCredentials(final TerraformTemplate terraformTemplate,
                                                   final CreateOrUpdateTerraformTemplateParams params);

    protected abstract void verifyUpdatePossible(final TerraformTemplate terraformTemplate,
                                                 final CreateOrUpdateTerraformTemplateParams params);

    @Override
    public void handleWebhookCallback(TerraformTemplate template, String payload, Map<String, String> headers) {
        final TerraformTemplateStorageInfo storageInfo = template.getStorageInfo();
        final Map<String, String> storageParams = storageInfo.getStorageParams();
        final String webhookActionString = storageParams.get(TerraformTemplateStorageInfo.Fields.WEBHOOK_ACTION);
        final WebHookAction webHookAction = WebHookAction.fromValue(webhookActionString);
        getLogger().info("Got webhook callback with {} webhook action", webHookAction);
        final ITerraformWebhookProcessor webhookProcessor = webhookProcessorProvider.provide(webHookAction);
        verifyWebhookCallbackIssuer(template, payload, headers);
        boolean proceedWithAction = validateEvent(template, headers);
        if (proceedWithAction) {
            webhookProcessor.handleWebhookAction(this, template);
        } else {
            getLogger().info("Webhook event does not need to be handled");
        }
    }

    protected abstract void verifyWebhookCallbackIssuer(final TerraformTemplate template,
                                                        final String payload,
                                                        final Map<String, String> headers);

    protected abstract boolean validateEvent(final TerraformTemplate template, final Map<String, String> headers);

    @Override
    public void autoPlanTemplate(final TerraformTemplate template) {
        planTemplate(template, "SYSTEM", Collections.emptyMap(), true);
    }

    @Override
    public void autoApplyTemplate(final TerraformTemplate template) {
        applyTemplate(template, "SYSTEM", Collections.emptyMap(), true, null);
    }

    @Override
    protected void clearStorageContent(final TerraformTemplate template) {
        deleteWebhook(template);
        internalStorageCaller.deleteTemplate(template);
    }
}
