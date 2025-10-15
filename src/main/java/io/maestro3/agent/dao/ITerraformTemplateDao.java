package io.maestro3.agent.dao;

import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.model.TerraformTemplateDbUpdateParameters;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface ITerraformTemplateDao {
    void delete(String templateId);

    Optional<TerraformTemplate> findByTemplateId(String templateId);

    Optional<TerraformTemplate> findByNameAndTenant(String name, String tenantName);

    Collection<TerraformTemplate> findByNamesAndTenant(String tenantName, Collection<String> names);

    TerraformTemplate save(TerraformTemplate terraformTemplate);

    boolean updateTemplateSet(String templateId, Map<String, Object> fieldsToUpdate);

    boolean updateTemplateUnset(String templateId, Collection<String> fieldsToUpdate);

    boolean updateTemplateAddToSet(String templateId, String field, Object valueToAdd);

    void pollQueuedTask(String templateId);

    Collection<TerraformTemplate> findTemplatesWithQueuedTasks();

    Optional<TerraformTemplate> findSystemTemplateByName(String templateName);

    String concatenateNestedField(String... fields);

    void updateTemplate(String templateId, TerraformTemplateDbUpdateParameters parameters);
}
