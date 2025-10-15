package io.maestro3.agent.terraform.model;

import team.syndicate.terraform.engine.management.model.TerraformTemplateInProgressTask;
import team.syndicate.terraform.engine.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateStorageInfo;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateVariable;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;

import java.util.Map;
import java.util.Set;

public class TerraformTemplateDbUpdateParameters {

    private UpdateParameter<TemplateStatus> newTemplateStatus;
    private UpdateParameter<Set<String>> newProviders;
    private UpdateParameter<Map<String, TerraformTemplateVariable>> newTemplateVariables;
    private UpdateParameter<Map<String, TerraformUserVariable>> newUserVariables;
    private UpdateParameter<TerraformTemplateInProgressTask> newTask;
    private UpdateParameter<String> newAutoTask;
    private UpdateParameter<TerraformTemplateStorageInfo> newStorageInfo;

    public UpdateParameter<TemplateStatus> getNewTemplateStatus() {
        return newTemplateStatus;
    }

    public TerraformTemplateDbUpdateParameters withNewTemplateStatus(TemplateStatus newTemplateStatus) {
        this.newTemplateStatus = new UpdateParameter<>(newTemplateStatus);
        return this;
    }

    public UpdateParameter<Set<String>> getNewProviders() {
        return newProviders;
    }

    public TerraformTemplateDbUpdateParameters withNewProviders(Set<String> newProviders) {
        this.newProviders = new UpdateParameter<>(newProviders);
        return this;
    }

    public UpdateParameter<Map<String, TerraformTemplateVariable>> getNewTemplateVariables() {
        return newTemplateVariables;
    }

    public TerraformTemplateDbUpdateParameters withNewTemplateVariables(Map<String, TerraformTemplateVariable> newTemplateVariables) {
        this.newTemplateVariables = new UpdateParameter<>(newTemplateVariables);
        return this;
    }

    public UpdateParameter<Map<String, TerraformUserVariable>> getNewUserVariables() {
        return newUserVariables;
    }

    public TerraformTemplateDbUpdateParameters withNewUserVariables(Map<String, TerraformUserVariable> newUserVariables) {
        this.newUserVariables = new UpdateParameter<>(newUserVariables);
        return this;
    }

    public UpdateParameter<TerraformTemplateInProgressTask> getNewTask() {
        return newTask;
    }

    public TerraformTemplateDbUpdateParameters withNewTask(TerraformTemplateInProgressTask newTask) {
        this.newTask = new UpdateParameter<>(newTask);
        return this;
    }

    public UpdateParameter<String> getNewAutoTask() {
        return newAutoTask;
    }

    public TerraformTemplateDbUpdateParameters withNewAutoTask(String newAutoTask) {
        this.newAutoTask = new UpdateParameter<>(newAutoTask);
        return this;
    }

    public UpdateParameter<TerraformTemplateStorageInfo> getNewStorageInfo() {
        return newStorageInfo;
    }

    public TerraformTemplateDbUpdateParameters withNewStorageInfo(TerraformTemplateStorageInfo newStorageInfo) {
        this.newStorageInfo = new UpdateParameter<>(newStorageInfo);
        return this;
    }

    public static class UpdateParameter<T> {
        private final T parameterValue;

        public UpdateParameter(T parameterValue) {
            this.parameterValue = parameterValue;
        }

        public T getParameterValue() {
            return parameterValue;
        }
    }
}
