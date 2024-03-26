package io.maestro3.agent.terraform.manager;

import io.maestro3.agent.terraform.model.TerraformTemplate;
import team.syndicate.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.integration.task.TerraformTask;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ITerraformTemplateService {
    boolean isTemplateExist(String name, String tenantName);

    void save(TerraformTemplate terraformTemplate);

    void delete(String templateId);

    Collection<TerraformTemplate> find(String tenantName, Collection<String> templateNames);

    Optional<TerraformTemplate> find(String tenantName, String templateName);

    Optional<TerraformTemplate> find(String templateId);

    void updateTemplateStatus(TerraformTemplate template, TemplateStatus newStatus);

    void updateTemplateStatus(String templateId, TemplateStatus newStatus);

    void updateTemplateProviders(String templateId, Set<String> newProviders);

    void addTaskInProgress(TerraformTemplate template, String taskId, TerraformTask task);

    void removeTask(String templateId, String taskId);

    void addTaskInQueue(TerraformTemplate template, String task);

    String pollQueuedTask(TerraformTemplate template);

    Collection<TerraformTemplate> findTemplatesWithQueuedTasks();
}
