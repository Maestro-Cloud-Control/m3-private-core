package io.maestro3.agent.dao;

import io.maestro3.agent.terraform.model.TerraformStack;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface ITerraformStackDao {

    Optional<TerraformStack> findByTemplateId(String templateId);

    Optional<TerraformStack> findByStackId(String stackId);

    void save(TerraformStack terraformStack);

    Collection<TerraformStack> findByTemplateName(String tenantName, String templateName);

    Optional<TerraformStack> findByTemplateName(String tenantName, String templateName, String stackId);

    void updateStack(String stackId, Map<String, Object> fieldsToUpdate);

    Collection<TerraformStack> findByTenant(String tenantName, Collection<String> stackIds);

    void delete(String stackId);

    Optional<TerraformStack> findByEntryID(String entryId);
}
