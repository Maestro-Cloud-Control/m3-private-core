package io.maestro3.agent.terraform.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import team.syndicate.terraform.engine.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Document(collection = "TerraformStacks")
public class TerraformStack {

    @Id
    private String stackId;
    @Field(Fields.CLOUD)
    @JsonProperty(Fields.CLOUD)
    private String cloud;
    @Field(Fields.TENANT_NAME)
    @JsonProperty(Fields.TENANT_NAME)
    private String tenantName;
    @Field(Fields.TEMPLATE_ID)
    @JsonProperty(Fields.TEMPLATE_ID)
    private String templateId;
    @Field(Fields.TEMPLATE_NAME)
    @JsonProperty(Fields.TEMPLATE_NAME)
    private String templateName;
    @Field(Fields.STATUS)
    @JsonProperty(Fields.STATUS)
    private TemplateStatus status;
    @Field(Fields.DESCRIPTION)
    @JsonProperty(Fields.DESCRIPTION)
    private String description;
    @Field(Fields.OWNER)
    @JsonProperty(Fields.OWNER)
    private String owner;
    @Field(Fields.PROVIDERS)
    @JsonProperty(Fields.PROVIDERS)
    private Set<String> providers;
    @Field(Fields.VARIABLES)
    @JsonProperty(Fields.VARIABLES)
    private Map<String, TerraformUserVariable> variables;
    @Field(Fields.SUB_DIRECTORY)
    @JsonProperty(Fields.SUB_DIRECTORY)
    private String subDirectory;
    @Field(Fields.SERVICE_ENTRY_ID)
    @JsonProperty(Fields.SERVICE_ENTRY_ID)
    private String serviceEntryId;

    public TerraformStack() {
        this.stackId = UUID.randomUUID().toString();
    }

    public String getStackId() {
        return stackId;
    }

    public String getCloud() {
        return cloud;
    }

    public TerraformStack withCloud(String cloud) {
        this.cloud = cloud;
        return this;
    }

    public String getTenantName() {
        return tenantName;
    }

    public TerraformStack withTenantName(String tenantName) {
        this.tenantName = tenantName;
        return this;
    }

    public String getTemplateId() {
        return templateId;
    }

    public TerraformStack withTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public String getTemplateName() {
        return templateName;
    }

    public TerraformStack withTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public TemplateStatus getStatus() {
        return status;
    }

    public TerraformStack withStatus(TemplateStatus status) {
        this.status = status;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TerraformStack withDescription(String description) {
        this.description = description;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public TerraformStack withOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public Set<String> getProviders() {
        return providers;
    }

    public TerraformStack withProviders(Set<String> providers) {
        this.providers = providers;
        return this;
    }

    public Map<String, TerraformUserVariable> getVariables() {
        return variables;
    }

    public TerraformStack withVariables(Map<String, TerraformUserVariable> variables) {
        this.variables = variables;
        return this;
    }

    public String getSubDirectory() {
        return subDirectory;
    }

    public TerraformStack withSubDirectory(String subDirectory) {
        this.subDirectory = subDirectory;
        return this;
    }

    public String getServiceEntryId() {
        return serviceEntryId;
    }

    public TerraformStack withServiceEntryId(String serviceEntryId) {
        this.serviceEntryId = serviceEntryId;
        return this;
    }

    public static final class Fields {
        public static final String STACK_ID = "stackId";
        public static final String CLOUD = "cloud";
        public static final String TENANT_NAME = "tenantName";
        public static final String TEMPLATE_ID = "templateId";
        public static final String TEMPLATE_NAME = "templateName";
        public static final String STATUS = "status";
        public static final String DESCRIPTION = "description";
        public static final String OWNER = "owner";
        public static final String PROVIDERS = "providers";
        public static final String VARIABLES = "variables";
        public static final String SUB_DIRECTORY = "subDirectory";
        public static final String SERVICE_ENTRY_ID = "serviceEntryId";

        private Fields() {
            throw new UnsupportedOperationException("Class is not designed for an instantiation");
        }
    }
}
