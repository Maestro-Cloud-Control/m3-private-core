package io.maestro3.agent.terraform.webhook.processor.impl;

import io.maestro3.agent.terraform.model.CreateOrUpdateTerraformTemplateParams;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.webhook.SupportsTerraformWebhook;
import io.maestro3.agent.terraform.webhook.processor.ITerraformWebhookProcessor;
import io.maestro3.sdk.v3.model.terraform.WebHookAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TerraformNoneWebhookProcessor implements ITerraformWebhookProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TerraformNoneWebhookProcessor.class);

    @Override
    public Long setupWebhook(SupportsTerraformWebhook manager, TerraformTemplate template, CreateOrUpdateTerraformTemplateParams params) {
        return null;
    }

    @Override
    public boolean needToDeleteOldWebhook(WebHookAction previousWebhookAction) {
        return previousWebhookAction != WebHookAction.NONE;
    }

    @Override
    public void handleWebhookAction(SupportsTerraformWebhook manager, TerraformTemplate template) {
        LOG.error("Got webhook callback for template {} with NONE webhook action", template.getTemplateId());
    }

    @Override
    public WebHookAction getWebhookAction() {
        return WebHookAction.NONE;
    }
}
