package io.maestro3.agent.terraform.storage.impl;

import io.maestro3.agent.terraform.storage.ITerraformTemplateStorageManager;
import io.maestro3.agent.terraform.storage.ITerraformTemplateStorageManagerProvider;
import io.maestro3.agent.terraform.webhook.SupportsTerraformWebhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.integration.model.TerraformTemplateStorageType;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TerraformTemplateStorageManagerProvider implements ITerraformTemplateStorageManagerProvider {

    private final Map<TerraformTemplateStorageType, ITerraformTemplateStorageManager> storageManagers;
    private final Map<TerraformTemplateStorageType, SupportsTerraformWebhook> supportWebhookStorageManagers;

    @Autowired
    public TerraformTemplateStorageManagerProvider(List<ITerraformTemplateStorageManager> storageManagers) {
        final Map<TerraformTemplateStorageType, ITerraformTemplateStorageManager> storageManagersMap =
                new EnumMap<>(TerraformTemplateStorageType.class);
        final Map<TerraformTemplateStorageType, SupportsTerraformWebhook> webhookManagersMap =
                new EnumMap<>(TerraformTemplateStorageType.class);
        for (ITerraformTemplateStorageManager manager : storageManagers) {
            final boolean supportsWebhook = manager instanceof SupportsTerraformWebhook;
            Collection<TerraformTemplateStorageType> supportedStorageTypes = manager.getSupportedStorageTypes();
            for (TerraformTemplateStorageType storageType : supportedStorageTypes) {
                storageManagersMap.put(storageType, manager);
                if (supportsWebhook) {
                    webhookManagersMap.put(storageType, (SupportsTerraformWebhook) manager);
                }
            }
        }
        this.storageManagers = Collections.unmodifiableMap(storageManagersMap);
        this.supportWebhookStorageManagers = Collections.unmodifiableMap(webhookManagersMap);
    }

    @Override
    public ITerraformTemplateStorageManager provideStorageManager(TerraformTemplateStorageType storageType) {
        return Optional.ofNullable(storageManagers.get(storageType))
                .orElseThrow(() -> new IllegalStateException(String.format("Storage manager for storage type %s not found", storageType)));
    }

    @Override
    public SupportsTerraformWebhook provideWebhookStorageManager(TerraformTemplateStorageType storageType) {
        return Optional.ofNullable(supportWebhookStorageManagers.get(storageType))
                .orElseThrow(() -> new IllegalStateException(String.format("Storage manager with webhook support for storage type %s not found", storageType)));
    }
}
