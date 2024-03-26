package io.maestro3.agent.terraform.storage.impl.manager;

import io.maestro3.agent.terraform.api.IApiUrlProvider;
import io.maestro3.agent.terraform.git.IGitManager;
import io.maestro3.agent.terraform.git.IGitManagerProvider;
import io.maestro3.agent.terraform.git.IGitSecretManager;
import io.maestro3.agent.terraform.manager.ITerraformStackService;
import io.maestro3.agent.terraform.manager.ITerraformTemplateService;
import io.maestro3.agent.terraform.model.CreateOrUpdateTerraformTemplateParams;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.sender.IRabbitMqTerraformMessageSender;
import io.maestro3.agent.terraform.storage.internal.ITerraformTemplateInternalStorageCaller;
import io.maestro3.agent.terraform.webhook.SupportsTerraformWebhook;
import io.maestro3.agent.terraform.webhook.processor.ITerraformWebhookProcessorProvider;
import io.maestro3.agent.tf.integration.TerraformPipeLineContext;
import io.maestro3.agent.tf.integration.model.ITerraformTemplateInfo;
import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageInfo;
import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageType;
import io.maestro3.agent.terraform.util.SecurityUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.model.terraform.WebHookAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class GitTerraformTemplateManager extends AbstractTerraformWebhookTemplateManager implements SupportsTerraformWebhook {

    private static final Logger LOG = LoggerFactory.getLogger(GitTerraformTemplateManager.class);

    private final IGitManagerProvider gitManagerProvider;
    private final IGitSecretManager gitSecretManager;

    @Autowired
    public GitTerraformTemplateManager(ITerraformTemplateService terraformTemplateService,
                                       ITerraformTemplateInternalStorageCaller internalStorageCaller,
                                       ITerraformStackService stackService,
                                       IRabbitMqTerraformMessageSender terraformMessageSender,
                                       ITerraformWebhookProcessorProvider webhookProcessorProvider,
                                       IApiUrlProvider apiUrlProvider,
                                       IGitManagerProvider gitManagerProvider,
                                       IGitSecretManager gitSecretManager) {
        super(terraformTemplateService, internalStorageCaller, stackService, terraformMessageSender,
                webhookProcessorProvider, apiUrlProvider);
        this.gitManagerProvider = gitManagerProvider;
        this.gitSecretManager = gitSecretManager;
    }

    private static Map<String, String> buildTemplateStorageParams(final CreateOrUpdateTerraformTemplateParams.GitStorageSpecificInfo gitStorageInfo) {
        final Map<String, String> storageParams = new HashMap<>(5);
        storageParams.put(TerraformTemplateStorageInfo.Fields.GIT_URL, gitStorageInfo.getGitUrl());
        storageParams.put(TerraformTemplateStorageInfo.Fields.GIT_BRANCH, gitStorageInfo.getGitBranch());
        storageParams.put(TerraformTemplateStorageInfo.Fields.GIT_SUB_DIRECTORY, gitStorageInfo.getGitSubFolder());
        final String webhookAction = WebHookAction.fromValue(gitStorageInfo.getWebHookAction()).name();
        storageParams.put(TerraformTemplateStorageInfo.Fields.WEBHOOK_ACTION, webhookAction);
        return storageParams;
    }

    @Override
    protected boolean needToDeleteOldWebhook(final TerraformTemplateStorageInfo oldStorageInfo,
                                             final TerraformTemplateStorageInfo newStorageInfo) {
        final Map<String, String> oldStorageParams = oldStorageInfo.getStorageParams();
        final String webhookId = oldStorageParams.get(TerraformTemplateStorageInfo.Fields.WEBHOOK_ID);
        if (StringUtils.isBlank(webhookId)) {
            return false;
        }

        boolean providerChanged = !Objects.equals(oldStorageInfo.getStorageType(), newStorageInfo.getStorageType());
        if (providerChanged) {
            return true;
        }

        final Map<String, String> newStorageParams = newStorageInfo.getStorageParams();
        return !Objects.equals(oldStorageParams.get(TerraformTemplateStorageInfo.Fields.GIT_URL),
                newStorageParams.get(TerraformTemplateStorageInfo.Fields.GIT_URL));
    }

    @Override
    protected boolean needToUpdateExistingWebhook(final TerraformTemplateStorageInfo oldStorageInfo,
                                                  final TerraformTemplateStorageInfo newStorageInfo) {
        final Map<String, String> oldStorageParams = oldStorageInfo.getStorageParams();
        final String webhookId = oldStorageParams.get(TerraformTemplateStorageInfo.Fields.WEBHOOK_ID);
        if (StringUtils.isBlank(webhookId)) {
            return false;
        }

        final Map<String, String> newStorageParams = newStorageInfo.getStorageParams();
        return !Objects.equals(oldStorageParams.get(TerraformTemplateStorageInfo.Fields.GIT_BRANCH),
                newStorageParams.get(TerraformTemplateStorageInfo.Fields.GIT_BRANCH));
    }

    @Override
    protected void updateExistingWebhook(final TerraformTemplate existingTemplate,
                                         final TerraformTemplateStorageInfo newStorageInfo) {
        final TerraformTemplateStorageInfo storageInfo = existingTemplate.getStorageInfo();
        final IGitManager gitManager = gitManagerProvider.provideManager(storageInfo.getStorageType());
        final Map<String, String> storageParams = storageInfo.getStorageParams();
        getLogger().info("Updating existing webhook");
        gitManager.updateWebhook(existingTemplate.getTemplateId(),
                storageParams.get(TerraformTemplateStorageInfo.Fields.GIT_URL),
                storageParams.get(TerraformTemplateStorageInfo.Fields.WEBHOOK_ID),
                newStorageInfo.getStorageParams().get(TerraformTemplateStorageInfo.Fields.GIT_BRANCH));
    }

    @Override
    protected TerraformTemplateStorageInfo validateStorageInfo(final TerraformTemplate terraformTemplate,
                                                               final CreateOrUpdateTerraformTemplateParams params) {
        final CreateOrUpdateTerraformTemplateParams.GitStorageSpecificInfo gitStorageInfo = getStorageInfo(params);
        final TerraformTemplateStorageType storageType = validateStorageType(gitStorageInfo);
        final IGitManager gitManager = gitManagerProvider.provideManager(storageType);

        gitManager.validateStorageInfo(gitStorageInfo.getGitUrl(), gitStorageInfo.getGitToken(),
                gitStorageInfo.getGitBranch(), gitStorageInfo.getGitSubFolder());
        LOG.info("Storage info is valid");

        final Map<String, String> storageParams = buildTemplateStorageParams(gitStorageInfo);
        return new TerraformTemplateStorageInfo().withStorageType(storageType)
                .withStorageParams(storageParams);
    }

    @Override
    protected void saveStorageCredentials(TerraformTemplate terraformTemplate, CreateOrUpdateTerraformTemplateParams params) {
        final CreateOrUpdateTerraformTemplateParams.GitStorageSpecificInfo gitStorageInfo = getStorageInfo(params);
        gitSecretManager.createSecret(terraformTemplate.getTemplateId(), gitStorageInfo.getGitToken().toCharArray());
        LOG.info("Git token saved to Vault");
    }

    @Override
    public Long setupWebhook(final TerraformTemplate terraformTemplate,
                             final CreateOrUpdateTerraformTemplateParams params) {
        final CreateOrUpdateTerraformTemplateParams.GitStorageSpecificInfo gitStorageInfo = getStorageInfo(params);
        final TerraformTemplateStorageType storageType = validateStorageType(gitStorageInfo);
        final IGitManager gitManager = gitManagerProvider.provideManager(storageType);

        final String callbackUrl = apiUrlProvider.generateWebhookCallbackUrl(terraformTemplate);
        return gitManager.setupWebhook(terraformTemplate.getTemplateId(), callbackUrl,
                gitStorageInfo.getGitToken(), gitStorageInfo.getGitUrl(), gitStorageInfo.getGitBranch());
    }

    @Override
    public void deleteWebhook(final TerraformTemplate template) {
        final TerraformTemplateStorageInfo storageInfo = template.getStorageInfo();
        final Map<String, String> storageParams = storageInfo.getStorageParams();
        final String repositoryUrl = storageParams.get(TerraformTemplateStorageInfo.Fields.GIT_URL);
        final String webhookId = storageParams.get(TerraformTemplateStorageInfo.Fields.WEBHOOK_ID);
        if (StringUtils.isBlank(webhookId)) {
            return;
        }

        final IGitManager gitManager = gitManagerProvider.provideManager(storageInfo.getStorageType());
        getLogger().info("Deleting webhook: {}", webhookId);
        gitManager.deleteWebhook(template.getTemplateId(), repositoryUrl, webhookId);
    }

    @Override
    protected void verifyUpdatePossible(final TerraformTemplate existingTemplate,
                                        final CreateOrUpdateTerraformTemplateParams params) {
        final TerraformTemplateStorageInfo storageInfo = existingTemplate.getStorageInfo();
        final TerraformTemplateStorageType templateStorageType = storageInfo.getStorageType();
        if (!templateStorageType.isGitBased()) {
            throw new IllegalStateException(
                    String.format(CHANGE_STORAGE_TYPE_ERROR_MESSAGE, existingTemplate.getName(), templateStorageType));
        }
    }

    @Override
    protected void clearStorageContent(final TerraformTemplate template) {
        super.clearStorageContent(template);
        gitSecretManager.deleteSecret(template.getTemplateId());
    }

    @Override
    protected File downloadTemplateFromStorage(TerraformPipeLineContext context, File targetDirectory) {
        ITerraformTemplateInfo template = context.getTemplate();
        TerraformTemplateStorageInfo storageInfo = template.getStorageInfo();
        TerraformTemplateStorageType storageType = storageInfo.getStorageType();
        Map<String, String> storageParams = storageInfo.getStorageParams();
        char[] token = gitSecretManager.getSecret(template.getTemplateId()).orElseThrow();
        String subDirectory = storageParams.get(TerraformTemplateStorageInfo.Fields.GIT_SUB_DIRECTORY);
        gitManagerProvider.provideManager(storageType)
                .clone(token, storageParams.get(TerraformTemplateStorageInfo.Fields.GIT_URL),
                        targetDirectory, storageParams.get(TerraformTemplateStorageInfo.Fields.GIT_BRANCH), subDirectory);
        SecurityUtils.clearSensitiveData(token);
        return getTemplateFile(targetDirectory, subDirectory);
    }

    private TerraformTemplateStorageType validateStorageType(final CreateOrUpdateTerraformTemplateParams.GitStorageSpecificInfo gitStorageInfo) {
        final TerraformTemplateStorageType storageType = gitStorageInfo.getStorageType();
        if (!storageType.isGitBased()) {
            throw new IllegalArgumentException(String.format("Specified provider %s is not a git-based provider", storageType));
        }
        return storageType;
    }

    private CreateOrUpdateTerraformTemplateParams.GitStorageSpecificInfo getStorageInfo(final CreateOrUpdateTerraformTemplateParams params) {
        CreateOrUpdateTerraformTemplateParams.StorageSpecificInfo storageSpecificInfo = params.getStorageInfo();
        if (storageSpecificInfo instanceof CreateOrUpdateTerraformTemplateParams.GitStorageSpecificInfo) {
            return (CreateOrUpdateTerraformTemplateParams.GitStorageSpecificInfo) storageSpecificInfo;
        }
        throw new IllegalArgumentException("Got malformed terraform template params");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public Collection<TerraformTemplateStorageType> getSupportedStorageTypes() {
        return Set.of(TerraformTemplateStorageType.GITHUB, TerraformTemplateStorageType.GITLAB);
    }

    @Override
    protected void verifyWebhookCallbackIssuer(final TerraformTemplate template,
                                               final String payload,
                                               final Map<String, String> headers) {
        final IGitManager gitManager = getGitManager(template);
        gitManager.verifyWebhookCallbackIssuer(template.getTemplateId(), payload, headers);
    }

    @Override
    protected boolean validateEvent(final TerraformTemplate template,
                                    final Map<String, String> headers) {
        final IGitManager gitManager = getGitManager(template);
        return gitManager.validateEvent(template.getTemplateId(), headers);
    }

    private IGitManager getGitManager(final TerraformTemplate template) {
        final TerraformTemplateStorageType storageType = template.getStorageInfo().getStorageType();
        return gitManagerProvider.provideManager(storageType);
    }
}
