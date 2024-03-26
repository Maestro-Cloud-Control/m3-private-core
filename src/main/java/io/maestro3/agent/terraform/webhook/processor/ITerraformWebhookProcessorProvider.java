package io.maestro3.agent.terraform.webhook.processor;

import io.maestro3.sdk.v3.model.terraform.WebHookAction;

@FunctionalInterface
public interface ITerraformWebhookProcessorProvider {
    ITerraformWebhookProcessor provide(WebHookAction webHookAction);
}
