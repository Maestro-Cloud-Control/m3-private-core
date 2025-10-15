package io.maestro3.agent.terraform.model;


import team.syndicate.terraform.engine.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateStorageInfo;

import java.util.Set;

public class TerraformTemplateDto {

    private final String templateId;
    private final String name;
    private final String description;
    private final String tenantName;
    private final String cloud;
    private final TemplateStatus templateStatus;
    private final String owner;
    private final boolean multiStack;
    private final Set<String> providers;
    private final TerraformTemplateStorageInfo storageInfo;
    private final String serviceEntryId;

    private TerraformTemplateDto(Builder builder) {
        this.templateId = builder.templateId;
        this.name = builder.name;
        this.description = builder.description;
        this.tenantName = builder.tenantName;
        this.cloud = builder.cloud;
        this.templateStatus = builder.templateStatus;
        this.owner = builder.owner;
        this.multiStack = builder.multiStack;
        this.providers = builder.providers;
        this.storageInfo = builder.storageInfo;
        this.serviceEntryId = builder.serviceEntryId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getCloud() {
        return cloud;
    }

    public TemplateStatus getTemplateStatus() {
        return templateStatus;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isMultiStack() {
        return multiStack;
    }

    public Set<String> getProviders() {
        return providers;
    }

    public TerraformTemplateStorageInfo getStorageInfo() {
        return storageInfo;
    }

    public String getServiceEntryId() {
        return serviceEntryId;
    }

    public static final class Builder {
        private String templateId;
        private String name;
        private String description;
        private String tenantName;
        private String cloud;
        private TemplateStatus templateStatus;
        private String owner;
        private boolean multiStack;
        private Set<String> providers;
        private TerraformTemplateStorageInfo storageInfo;
        private String serviceEntryId;

        public Builder withTemplateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withTenantName(String tenantName) {
            this.tenantName = tenantName;
            return this;
        }

        public Builder withCloud(String cloud) {
            this.cloud = cloud;
            return this;
        }

        public Builder withTemplateStatus(TemplateStatus templateStatus) {
            this.templateStatus = templateStatus;
            return this;
        }

        public Builder withOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder withMultiStack(boolean multiStack) {
            this.multiStack = multiStack;
            return this;
        }

        public Builder withProviders(Set<String> providers) {
            this.providers = providers;
            return this;
        }

        public Builder withStorageInfo(TerraformTemplateStorageInfo storageInfo) {
            this.storageInfo = storageInfo;
            return this;
        }

        public Builder withServiceEntryId(String serviceEntryId) {
            this.serviceEntryId = serviceEntryId;
            return this;
        }

        public TerraformTemplateDto build() {
            return new TerraformTemplateDto(this);
        }
    }
}
