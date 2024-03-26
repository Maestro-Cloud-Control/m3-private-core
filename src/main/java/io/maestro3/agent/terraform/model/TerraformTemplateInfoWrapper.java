package io.maestro3.agent.terraform.model;

import team.syndicate.terraform.integration.model.ITerraformTemplateInfo;
import team.syndicate.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.integration.model.TerraformTemplateStorageInfo;
import team.syndicate.terraform.integration.model.TerraformTemplateVariable;
import team.syndicate.terraform.integration.model.TerraformUserVariable;

import java.util.Map;
import java.util.Set;

public class TerraformTemplateInfoWrapper implements ITerraformTemplateInfo {

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
    public String getCloud() {
        return template.getCloud();
    }

    @Override
    public TemplateStatus getStatus() {
        return template.getStatus();
    }

    @Override
    public void setStatus(TemplateStatus status) {
        template.withStatus(status);
    }

    @Override
    public String getOwner() {
        return template.getOwner();
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
    public void setProviders(Set<String> providers) {
        template.withProviders(providers);
    }

    @Override
    public String getTemplateFileName() {
        return template.getTemplateFileName();
    }

    @Override
    public Map<String, TerraformUserVariable> getVariables() {
        return template.getVariables();
    }

    @Override
    public Map<String, TerraformTemplateVariable> getTemplateVariables() {
        return template.getTemplateVariables();
    }

    @Override
    public void setTemplateVariables(Map<String, TerraformTemplateVariable> templateVariables) {
        template.withTemplateVariables(templateVariables);
    }

    @Override
    public void setUserVariables(Map<String, TerraformUserVariable> userVariables) {
        template.withVariables(userVariables);
    }

    @Override
    public TerraformTemplateStorageInfo getStorageInfo() {
        return template.getStorageInfo();
    }
}
