package io.maestro3.agent.terraform.model;


import team.syndicate.terraform.engine.terraform.integration.model.ITerraformStackInfo;
import team.syndicate.terraform.engine.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;

import java.util.Map;
import java.util.Set;

public class TerraformStackInfoWrapper implements ITerraformStackInfo {

    private final TerraformStack stack;

    public TerraformStackInfoWrapper(TerraformStack stack) {
        this.stack = stack;
    }

    @Override
    public String getStackId() {
        return stack.getStackId();
    }

    @Override
    public TemplateStatus getStatus() {
        return stack.getStatus();
    }

    @Override
    public void setStatus(TemplateStatus status) {
        stack.withStatus(status);
    }

    @Override
    public Set<String> getProviders() {
        return stack.getProviders();
    }

    @Override
    public void setProviders(Set<String> providers) {
        stack.withProviders(providers);
    }

    @Override
    public Map<String, TerraformUserVariable> getVariables() {
        return stack.getVariables();
    }

    @Override
    public void setVariables(Map<String, TerraformUserVariable> variables) {
        stack.withVariables(variables);
    }

    @Override
    public String getSubDirectory() {
        return stack.getSubDirectory();
    }

    @Override
    public void setSubDirectory(String subDirectory) {
        stack.withSubDirectory(subDirectory);
    }

    public String getServiceEntryId() {
        return stack.getServiceEntryId();
    }

}