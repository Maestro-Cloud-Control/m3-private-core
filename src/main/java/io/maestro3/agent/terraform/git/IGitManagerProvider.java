package io.maestro3.agent.terraform.git;

import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageType;

@FunctionalInterface
public interface IGitManagerProvider {
    IGitManager provideManager(TerraformTemplateStorageType storageType);
}
