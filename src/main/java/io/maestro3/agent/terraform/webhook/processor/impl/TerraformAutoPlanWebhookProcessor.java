package io.maestro3.agent.terraform.webhook.processor.impl;

import io.maestro3.agent.terraform.exception.ConcurrentActionException;
import io.maestro3.agent.terraform.manager.ITerraformTemplateService;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.webhook.SupportsTerraformWebhook;
import io.maestro3.agent.terraform.webhook.processor.ITerraformWebhookProcessor;
import io.maestro3.sdk.v3.model.terraform.WebHookAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TerraformAutoPlanWebhookProcessor implements ITerraformWebhookProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TerraformAutoPlanWebhookProcessor.class);

    private final ITerraformTemplateService terraformTemplateService;

    @Autowired
    public TerraformAutoPlanWebhookProcessor(ITerraformTemplateService terraformTemplateService) {
        this.terraformTemplateService = terraformTemplateService;
    }

    @Override
    public void handleWebhookAction(final SupportsTerraformWebhook manager, final TerraformTemplate template) {
        try {
            manager.autoPlanTemplate(template);
        } catch (ConcurrentActionException e) {
            terraformTemplateService.addTaskInQueue(template, getWebhookAction().name());
            LOG.info("Webhook action 'plan' was queued for execution");
        }
    }

    @Override
    public WebHookAction getWebhookAction() {
        return WebHookAction.AUTO_PLAN;
    }
}
