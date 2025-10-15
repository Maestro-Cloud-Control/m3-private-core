package io.maestro3.agent.platform.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.management.interfaces.webhook.IApiUrlProvider;
import team.syndicate.terraform.engine.terraform.integration.model.ITerraformTemplateInfo;

@Service
public class ApiUrlProvider implements IApiUrlProvider {

    private static final String CALLBACK_URL_PATTERN = "%s%s?%s=%s";

    private final String host;
    private final String webhookControllerSuffix;

    @Autowired
    public ApiUrlProvider(@Value("${webhook.api.host}") String host,
                          @Value("${terraform.webhook.controller.suffix}") String webhookControllerSuffix) {
        this.host = host;
        this.webhookControllerSuffix = webhookControllerSuffix;
    }

    @Override
    public String generateWebhookCallbackUrl(final ITerraformTemplateInfo terraformTemplate) {
        return String.format(CALLBACK_URL_PATTERN, host, webhookControllerSuffix,
                "templateId", terraformTemplate.getTemplateId());
    }
}
