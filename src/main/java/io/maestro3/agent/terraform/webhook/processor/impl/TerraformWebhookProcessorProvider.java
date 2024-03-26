package io.maestro3.agent.terraform.webhook.processor.impl;

import io.maestro3.agent.terraform.webhook.processor.ITerraformWebhookProcessor;
import io.maestro3.agent.terraform.webhook.processor.ITerraformWebhookProcessorProvider;
import io.maestro3.sdk.v3.model.terraform.WebHookAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TerraformWebhookProcessorProvider implements ITerraformWebhookProcessorProvider {

    private final Map<WebHookAction, ITerraformWebhookProcessor> webhookProcessors;

    @Autowired
    public TerraformWebhookProcessorProvider(List<ITerraformWebhookProcessor> webhookProcessors) {
        this.webhookProcessors = webhookProcessors.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(ITerraformWebhookProcessor::getWebhookAction, Function.identity()),
                        Collections::unmodifiableMap));
    }

    @Override
    public ITerraformWebhookProcessor provide(WebHookAction webHookAction) {
        return Optional.ofNullable(webhookProcessors.get(webHookAction))
                .orElseThrow(() -> new IllegalStateException(String.format("Processor for webhook action %s not found", webHookAction)));
    }
}
