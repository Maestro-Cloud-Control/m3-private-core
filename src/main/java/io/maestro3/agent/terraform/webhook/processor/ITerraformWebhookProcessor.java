package io.maestro3.agent.terraform.webhook.processor;

import io.maestro3.agent.terraform.model.CreateOrUpdateTerraformTemplateParams;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.webhook.SupportsTerraformWebhook;
import io.maestro3.sdk.v3.model.terraform.WebHookAction;

public interface ITerraformWebhookProcessor {

    default Long setupWebhook(SupportsTerraformWebhook manager, TerraformTemplate template, CreateOrUpdateTerraformTemplateParams params) {
        if (template.isMultiStack()) {
            throw new IllegalStateException("Webhook action can not be executed for multi stack template");
        }
        return manager.setupWebhook(template, params);
    }

    default boolean needToDeleteOldWebhook(WebHookAction previousWebhookAction) {
        return false;
    }

    void handleWebhookAction(SupportsTerraformWebhook manager, TerraformTemplate template);

    WebHookAction getWebhookAction();
}
