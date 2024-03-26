package io.maestro3.agent.terraform.git.impl;

import io.maestro3.agent.terraform.git.IGitManager;
import io.maestro3.agent.terraform.git.IGitManagerProvider;
import io.maestro3.agent.terraform.git.IGitSecretManager;
import io.maestro3.agent.terraform.git.IGitWebhookSecretManager;
import io.maestro3.agent.terraform.git.manager.GitHubManager;
import io.maestro3.agent.terraform.git.manager.GitLabManager;
import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class GitManagerProvider implements IGitManagerProvider {

    private final Map<TerraformTemplateStorageType, IGitManager> gitProviders;

    @Autowired
    public GitManagerProvider(IGitSecretManager gitSecretManager,
                              IGitWebhookSecretManager webhookSecretManager) {
        gitProviders = Map.of(
                TerraformTemplateStorageType.GITHUB, new GitHubManager(gitSecretManager, webhookSecretManager, TerraformTemplateStorageType.GITHUB.name()),
                TerraformTemplateStorageType.GITLAB, new GitLabManager(gitSecretManager, webhookSecretManager, TerraformTemplateStorageType.GITLAB.name())
        );
    }

    @Override
    public IGitManager provideManager(final TerraformTemplateStorageType storageType) {
        if (!storageType.isGitBased()) {
            throw new IllegalArgumentException(String.format("Storage type %s is not a Git-based storage", storageType));
        }

        return Optional.ofNullable(gitProviders.get(storageType))
                .orElseThrow(() -> new IllegalArgumentException("There is no manager for git provider " + storageType));
    }
}
