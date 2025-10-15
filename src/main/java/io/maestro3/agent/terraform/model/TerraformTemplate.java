package io.maestro3.agent.terraform.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import team.syndicate.terraform.engine.terraform.integration.model.TemplateFormat;
import team.syndicate.terraform.engine.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateStorageInfo;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateVariable;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;
import team.syndicate.terraform.engine.terraform.integration.task.TerraformTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Document(collection = "TerraformTemplates")
public class TerraformTemplate {

    @Id
    private String templateId;
    @Field(Fields.NAME)
    @JsonProperty(Fields.NAME)
    private String name;
    @Field(Fields.DESCRIPTION)
    @JsonProperty(Fields.DESCRIPTION)
    private String description;
    @Field(Fields.TENANT_NAME)
    @JsonProperty(Fields.TENANT_NAME)
    private String tenantName;
    @Field(Fields.TENANT_DISPLAY_NAME)
    @JsonProperty(Fields.TENANT_DISPLAY_NAME)
    private String tenantDisplayName;
    @Field(Fields.CLOUD)
    @JsonProperty(Fields.CLOUD)
    private String cloud;
    @Field(Fields.STATUS)
    @JsonProperty(Fields.STATUS)
    private TemplateStatus status;
    @Field(Fields.FORMAT)
    @JsonProperty(Fields.FORMAT)
    private TemplateFormat format;
    @Field(Fields.OWNER)
    @JsonProperty(Fields.OWNER)
    private String owner;
    @Field(Fields.MULTI_STACK)
    @JsonProperty(Fields.MULTI_STACK)
    private boolean multiStack;
    @Field(Fields.PROVIDERS)
    @JsonProperty(Fields.PROVIDERS)
    private Set<String> providers;
    @Field(Fields.TASKS_IN_PROGRESS)
    @JsonProperty(Fields.TASKS_IN_PROGRESS)
    private Map<String, TerraformTask> tasksInProgress = new ConcurrentHashMap<>();
    @Field(Fields.AUTO_TASK_QUEUE)
    @JsonProperty(Fields.AUTO_TASK_QUEUE)
    private ConcurrentLinkedQueue<String> autoTaskQueue = new ConcurrentLinkedQueue<>();
    @Field(Fields.VARIABLES)
    @JsonProperty(Fields.VARIABLES)
    private Map<String, TerraformUserVariable> variables = new HashMap<>();
    @Field(Fields.TEMPLATE_VARIABLES)
    @JsonProperty(Fields.TEMPLATE_VARIABLES)
    private Map<String, TerraformTemplateVariable> templateVariables = new HashMap<>();
    @Field(Fields.STORAGE_INFO)
    @JsonProperty(Fields.STORAGE_INFO)
    private TerraformTemplateStorageInfo storageInfo;
    @Field(Fields.SYSTEM)
    @JsonProperty(Fields.SYSTEM)
    private boolean system;

    public TerraformTemplate() {
        this.templateId = UUID.randomUUID().toString();
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getName() {
        return name;
    }

    public TerraformTemplate withName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TerraformTemplate withDescription(String description) {
        this.description = description;
        return this;
    }

    public String getTenantName() {
        return tenantName;
    }

    public TerraformTemplate withTenantName(String tenantName) {
        this.tenantName = tenantName;
        return this;
    }

    public String getTenantDisplayName() {
        return tenantDisplayName;
    }

    public TerraformTemplate withTenantDisplayName(String tenantDisplayName) {
        this.tenantDisplayName = tenantDisplayName;
        return this;
    }

    public String getCloud() {
        return cloud;
    }

    public TerraformTemplate withCloud(String cloud) {
        this.cloud = cloud;
        return this;
    }

    public TemplateStatus getStatus() {
        return status;
    }

    public TerraformTemplate withStatus(TemplateStatus status) {
        this.status = status;
        return this;
    }

    public TemplateFormat getFormat() {
        return format;
    }

    public TerraformTemplate withFormat(TemplateFormat format) {
        this.format = format;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public TerraformTemplate withOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public boolean isMultiStack() {
        return multiStack;
    }

    public TerraformTemplate withMultiStack(boolean multiStack) {
        this.multiStack = multiStack;
        return this;
    }

    public Set<String> getProviders() {
        return providers;
    }

    public TerraformTemplate withProviders(Set<String> providers) {
        this.providers = providers;
        return this;
    }

    public Map<String, TerraformTask> getTasksInProgress() {
        return tasksInProgress;
    }

    public void addTaskInProgress(String taskId, TerraformTask task) {
        tasksInProgress.put(taskId, task);
    }

    public boolean isTaskInProgress(TerraformTask task) {
        return tasksInProgress.containsValue(task);
    }

    public Queue<String> getAutoTaskQueue() {
        return autoTaskQueue;
    }

    public void addTaskToQueue(String task) {
        if (isTaskInQueue(task)) {
            return;
        }
        autoTaskQueue.add(task);
    }

    public boolean isTaskInQueue(String task) {
        return autoTaskQueue.contains(task);
    }

    public String pollTaskFromQueue() {
        return autoTaskQueue.poll();
    }

    public Map<String, TerraformUserVariable> getVariables() {
        return variables;
    }

    public TerraformTemplate withVariables(Map<String, TerraformUserVariable> variables) {
        this.variables = variables;
        return this;
    }

    public Map<String, TerraformTemplateVariable> getTemplateVariables() {
        return templateVariables;
    }

    public TerraformTemplate withTemplateVariables(Map<String, TerraformTemplateVariable> templateVariables) {
        this.templateVariables = templateVariables;
        return this;
    }

    public TerraformTemplateStorageInfo getStorageInfo() {
        return storageInfo;
    }

    public TerraformTemplate withStorageInfo(TerraformTemplateStorageInfo storageInfo) {
        this.storageInfo = storageInfo;
        return this;
    }

    public boolean isSystem() {
        return system;
    }

    public TerraformTemplate withSystem(boolean system) {
        this.system = system;
        return this;
    }

    public String getTemplateFileName() {
        return name + format.getFileExtension();
    }

    public static final class Fields {
        public static final String TEMPLATE_ID = "templateId";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String TENANT_NAME = "tenantName";
        public static final String TENANT_DISPLAY_NAME = "tenantDisplayName";
        public static final String CLOUD = "cloud";
        public static final String STATUS = "status";
        public static final String FORMAT = "format";
        public static final String OWNER = "owner";
        public static final String MULTI_STACK = "multiStack";
        public static final String PROVIDERS = "providers";
        public static final String TASKS_IN_PROGRESS = "tasksInProgress";
        public static final String AUTO_TASK_QUEUE = "autoTaskQueue";
        public static final String VARIABLES = "variables";
        public static final String TEMPLATE_VARIABLES = "templateVariables";
        public static final String STORAGE_INFO = "storageInfo";
        public static final String SYSTEM = "system";

        private Fields() {
            throw new UnsupportedOperationException("Class is not designed for an instantiation");
        }
    }
}
