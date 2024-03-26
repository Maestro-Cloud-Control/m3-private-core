package io.maestro3.agent.terraform.webhook.impl;

import io.maestro3.agent.terraform.exception.BadRequestException;
import io.maestro3.agent.terraform.exception.ResourceNotFoundException;
import io.maestro3.agent.terraform.manager.ITerraformTemplateService;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.storage.ITerraformTemplateStorageManagerProvider;
import io.maestro3.agent.terraform.webhook.ITerraformWebhookHandler;
import io.maestro3.agent.terraform.webhook.SupportsTerraformWebhook;
import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageInfo;
import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageType;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class TerraformWebhookHandler implements ITerraformWebhookHandler {

    private final ITerraformTemplateService terraformTemplateService;
    private final ITerraformTemplateStorageManagerProvider storageManagerProvider;

    @Autowired
    public TerraformWebhookHandler(ITerraformTemplateService terraformTemplateService,
                                   ITerraformTemplateStorageManagerProvider storageManagerProvider) {
        this.terraformTemplateService = terraformTemplateService;
        this.storageManagerProvider = storageManagerProvider;
    }

    @Override
    public void handleWebhook(final String payload, final String templateId, final Map<String, String> headers) {
        final TerraformTemplate template = getTemplate(templateId);
        MDC.put("activity", "webhook callback");
        MDC.put("templateId", template.getTemplateId());
        try {
            handleWebhook(template, payload, headers);
        } finally {
            MDC.clear();
        }
    }

    private void handleWebhook(final TerraformTemplate template, final String payload, final Map<String, String> headers) {
        final TerraformTemplateStorageInfo storageInfo = template.getStorageInfo();
        final TerraformTemplateStorageType storageType = storageInfo.getStorageType();
        if (!storageType.isSupportWebhook()) {
            throw new IllegalStateException(String.format(
                    "Got webhook callback for template with storage type %s which does not support webhooks", storageType));
        }

        final SupportsTerraformWebhook webhookStorageManager = storageManagerProvider.provideWebhookStorageManager(storageType);
        webhookStorageManager.handleWebhookCallback(template, payload, headers);
    }

    private TerraformTemplate getTemplate(final String templateId) {
        final boolean templateIdValid = validateTemplateId(templateId);
        if (!templateIdValid) {
            throw new BadRequestException("Got malformed template ID: " + templateId);
        }

        return terraformTemplateService.find(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Terraform template", templateId));
    }

    private boolean validateTemplateId(final String templateId) {
        try {
            UUID.fromString(templateId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
