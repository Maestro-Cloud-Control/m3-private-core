package io.maestro3.agent.terraform.model;

import team.syndicate.terraform.engine.management.interfaces.model.ITerraformEngineTemplateInfo;
import team.syndicate.terraform.engine.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateStorageInfo;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateVariable;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;
import team.syndicate.terraform.engine.terraform.integration.task.TerraformTask;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class TerraformTemplateInfoWrapper implements ITerraformEngineTemplateInfo {

    private final TerraformTemplate template;

    public TerraformTemplateInfoWrapper(TerraformTemplate template) {
        this.template = template;
    }

    @Override
    public String getTemplateId() {
        return template.getTemplateId();
    }

    @Override
    public String getName() {
        return template.getName();
    }

    @Override
    public String getDescription() {
        return template.getDescription();
    }

    @Override
    public String getTenantName() {
        return template.getTenantName();
    }

    @Override
    public String getTenantDisplayName() {
        return template.getTenantDisplayName();
    }

    @Override
    public String getCloud() {
        return template.getCloud();
    }

    @Override
    public String getOwner() {
        return template.getOwner();
    }

    @Override
    public TemplateStatus getStatus() {
        return template.getStatus();
    }

    @Override
    public Map<String, TerraformTask> getTasksInProgress() {
        return template.getTasksInProgress();
    }

    @Override
    public Queue<String> getAutoTaskQueue() {
        return template.getAutoTaskQueue();
    }

    @Override
    public boolean isMultiStack() {
        return template.isMultiStack();
    }

    @Override
    public Set<String> getProviders() {
        return template.getProviders();
    }

    @Override
    public String getTemplateFileName() {
        return template.getTemplateFileName();
    }

    @Override
    public Map<String, TerraformUserVariable> getUserVariables() {
        return template.getVariables();
    }

    @Override
    public Map<String, TerraformTemplateVariable> getTemplateVariables() {
        return template.getTemplateVariables();
    }

    @Override
    public String buildLockIdentifier() {
        return "TerraformTemplate-" + getTemplateId();
    }

    @Override
    public TerraformTemplateStorageInfo getStorageInfo() {
        return template.getStorageInfo();
    }
}
