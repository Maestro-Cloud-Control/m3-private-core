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
public class TerraformAutoApplyWebhookProcessor implements ITerraformWebhookProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TerraformAutoApplyWebhookProcessor.class);

    private final ITerraformTemplateService terraformTemplateService;

    @Autowired
    public TerraformAutoApplyWebhookProcessor(ITerraformTemplateService terraformTemplateService) {
        this.terraformTemplateService = terraformTemplateService;
    }

    @Override
    public void handleWebhookAction(SupportsTerraformWebhook manager, TerraformTemplate template) {
        try {
            manager.autoApplyTemplate(template);
        } catch (ConcurrentActionException e) {
            terraformTemplateService.addTaskInQueue(template, getWebhookAction().name());
            LOG.info("Webhook action 'apply' was queued for execution");
        }
    }

    @Override
    public WebHookAction getWebhookAction() {
        return WebHookAction.AUTO_APPLY;
    }
}
