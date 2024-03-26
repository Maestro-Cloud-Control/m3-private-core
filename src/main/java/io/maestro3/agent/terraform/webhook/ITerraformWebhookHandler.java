package io.maestro3.agent.terraform.webhook;

import java.util.Map;

public interface ITerraformWebhookHandler {
    void handleWebhook(String payload, String templateId, Map<String, String> headers);
}
