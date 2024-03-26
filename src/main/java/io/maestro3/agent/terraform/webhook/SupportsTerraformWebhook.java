package io.maestro3.agent.terraform.webhook;

import io.maestro3.agent.terraform.model.CreateOrUpdateTerraformTemplateParams;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.storage.ITerraformTemplateStorageManager;

import java.util.Map;

public interface SupportsTerraformWebhook extends ITerraformTemplateStorageManager {

    Long setupWebhook(TerraformTemplate template, CreateOrUpdateTerraformTemplateParams params);

    void handleWebhookCallback(TerraformTemplate template, String payload, Map<String, String> headers);

    void deleteWebhook(TerraformTemplate template);

    void autoPlanTemplate(TerraformTemplate template);

    void autoApplyTemplate(TerraformTemplate template);
}
