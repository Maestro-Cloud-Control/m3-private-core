package io.maestro3.agent.terraform.model;

import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageType;
import io.maestro3.agent.tf.integration.model.TerraformVariable;

import java.util.Map;

public class CreateOrUpdateTerraformTemplateParams {

    private final String templateName;
    private final String description;
    private final boolean multiStack;
    private final Map<String, TerraformVariable> variables;
    private final StorageSpecificInfo storageSpecificInfo;

    private CreateOrUpdateTerraformTemplateParams(InternalStorageParamsBuilder internalStorageParamsBuilder) {
        Builder builder = internalStorageParamsBuilder.builder;
        this.templateName = builder.templateName;
        this.description = builder.description;
        this.multiStack = builder.multiStack;
        this.variables = builder.variables;
        this.storageSpecificInfo = new InternalStorageSpecificInfo(internalStorageParamsBuilder);
    }

    private CreateOrUpdateTerraformTemplateParams(GitStorageParamsBuilder gitStorageParamsBuilder) {
        Builder builder = gitStorageParamsBuilder.builder;
        this.templateName = builder.templateName;
        this.description = builder.description;
        this.multiStack = builder.multiStack;
        this.variables = builder.variables;
        this.storageSpecificInfo = new GitStorageSpecificInfo(gitStorageParamsBuilder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMultiStack() {
        return multiStack;
    }

    public Map<String, TerraformVariable> getVariables() {
        return variables;
    }

    public StorageSpecificInfo getStorageInfo() {
        return storageSpecificInfo;
    }

    public static class StorageSpecificInfo {

        private final TerraformTemplateStorageType storageType;

        StorageSpecificInfo(TerraformTemplateStorageType storageType) {
            this.storageType = storageType;
        }

        public TerraformTemplateStorageType getStorageType() {
            return storageType;
        }
    }

    public static class SupportWebhookStorageInfo extends StorageSpecificInfo {
        private final String webHookAction;

        SupportWebhookStorageInfo(TerraformTemplateStorageType storageType,
                                  String webHookAction) {
            super(storageType);
            this.webHookAction = webHookAction;
        }

        public String getWebHookAction() {
            return webHookAction;
        }
    }

    public static final class InternalStorageSpecificInfo extends StorageSpecificInfo {
        private final String templateContent;

        private InternalStorageSpecificInfo(InternalStorageParamsBuilder builder) {
            super(TerraformTemplateStorageType.INTERNAL_STORAGE);
            this.templateContent = builder.templateContent;
        }

        public String getTemplateContent() {
            return templateContent;
        }
    }

    public static final class GitStorageSpecificInfo extends SupportWebhookStorageInfo {
        private final String gitUrl;
        private final String gitToken;
        private final String gitBranch;
        private final String gitSubFolder;

        private GitStorageSpecificInfo(GitStorageParamsBuilder builder) {
            super(builder.gitProvider, builder.gitWebHookAction);
            this.gitUrl = builder.gitUrl;
            this.gitToken = builder.gitToken;
            this.gitBranch = builder.gitBranch;
            this.gitSubFolder = builder.gitSubFolder;
        }

        public String getGitUrl() {
            return gitUrl;
        }

        public String getGitToken() {
            return gitToken;
        }

        public String getGitBranch() {
            return gitBranch;
        }

        public String getGitSubFolder() {
            return gitSubFolder;
        }
    }

    public static final class Builder {
        private String templateName;
        private String description;
        private boolean multiStack;
        private Map<String, TerraformVariable> variables;

        public Builder withTemplateName(String templateName) {
            this.templateName = templateName;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withMultiStack(boolean multiStack) {
            this.multiStack = multiStack;
            return this;
        }

        public Builder withVariables(Map<String, TerraformVariable> variables) {
            this.variables = variables;
            return this;
        }

        public InternalStorageParamsBuilder withInternalStorage() {
            return new InternalStorageParamsBuilder(this);
        }

        public GitStorageParamsBuilder withGitStorage() {
            return new GitStorageParamsBuilder(this);
        }
    }

    public static final class InternalStorageParamsBuilder {

        private final Builder builder;
        private String templateContent;

        private InternalStorageParamsBuilder(Builder builder) {
            this.builder = builder;
        }

        public InternalStorageParamsBuilder withTemplateContent(String templateContent) {
            this.templateContent = templateContent;
            return this;
        }

        public CreateOrUpdateTerraformTemplateParams build() {
            return new CreateOrUpdateTerraformTemplateParams(this);
        }
    }

    public static final class GitStorageParamsBuilder {

        private final Builder builder;
        private TerraformTemplateStorageType gitProvider;
        private String gitUrl;
        private String gitToken;
        private String gitBranch;
        private String gitSubFolder;
        private String gitWebHookAction;

        private GitStorageParamsBuilder(Builder builder) {
            this.builder = builder;
        }

        public GitStorageParamsBuilder withGitProvider(TerraformTemplateStorageType gitProvider) {
            this.gitProvider = gitProvider;
            return this;
        }

        public GitStorageParamsBuilder withGitUrl(String gitUrl) {
            this.gitUrl = gitUrl;
            return this;
        }

        public GitStorageParamsBuilder withGitToken(String gitToken) {
            this.gitToken = gitToken;
            return this;
        }

        public GitStorageParamsBuilder withGitBranch(String gitBranch) {
            this.gitBranch = gitBranch;
            return this;
        }

        public GitStorageParamsBuilder withGitSubFolder(String gitSubFolder) {
            this.gitSubFolder = gitSubFolder;
            return this;
        }

        public GitStorageParamsBuilder withGitWebHookAction(String gitWebHookAction) {
            this.gitWebHookAction = gitWebHookAction;
            return this;
        }

        public CreateOrUpdateTerraformTemplateParams build() {
            return new CreateOrUpdateTerraformTemplateParams(this);
        }
    }
}
