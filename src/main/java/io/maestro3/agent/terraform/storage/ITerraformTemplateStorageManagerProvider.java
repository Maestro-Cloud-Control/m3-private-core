package io.maestro3.agent.terraform.storage;

import io.maestro3.agent.terraform.webhook.SupportsTerraformWebhook;
import team.syndicate.terraform.integration.model.TerraformTemplateStorageType;

public interface ITerraformTemplateStorageManagerProvider {
    ITerraformTemplateStorageManager provideStorageManager(TerraformTemplateStorageType storageType);

    SupportsTerraformWebhook provideWebhookStorageManager(TerraformTemplateStorageType storageType);
}
