package io.maestro3.agent.terraform.sevice.impl;

import io.maestro3.agent.dao.ITerraformTemplateDao;
import io.maestro3.agent.terraform.sevice.ITerraformTemplateService;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.model.TerraformTemplateDbUpdateParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.utils.Asserts;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
public class TerraformTemplateService implements ITerraformTemplateService {

    private final ITerraformTemplateDao terraformTemplateDao;

    @Autowired
    public TerraformTemplateService(ITerraformTemplateDao terraformTemplateDao) {
        this.terraformTemplateDao = terraformTemplateDao;
    }

    @Override
    public boolean isTemplateExist(String name, String tenantName) {
        Asserts.notBlank(name, "name");
        Asserts.notBlank(tenantName, "tenantName");

        return terraformTemplateDao.findByNameAndTenant(name, tenantName).isPresent();
    }

    @Override
    public void save(TerraformTemplate terraformTemplate) {
        Asserts.notNull(terraformTemplate, "terraformTemplate");
        terraformTemplateDao.save(terraformTemplate);
    }

    @Override
    public void delete(String templateId) {
        Asserts.notNull(templateId, "templateId");
        terraformTemplateDao.delete(templateId);
    }

    @Override
    public Collection<TerraformTemplate> find(String tenantName, Collection<String> templateNames) {
        Asserts.notBlank(tenantName, "tenantName");
        return terraformTemplateDao.findByNamesAndTenant(tenantName, templateNames);
    }

    @Override
    public Optional<TerraformTemplate> find(String tenantName, String templateName) {
        Asserts.notBlank(tenantName, "tenantName");
        Asserts.notBlank(templateName, "templateName");
        return terraformTemplateDao.findByNameAndTenant(templateName, tenantName);
    }

    @Override
    public Optional<TerraformTemplate> find(String templateId) {
        Asserts.notBlank(templateId, "templateId");
        return terraformTemplateDao.findByTemplateId(templateId);
    }

    @Override
    public void removeTask(String templateId, String taskId) {
        Asserts.notBlank(templateId, "templateId");
        Asserts.notBlank(taskId, "taskId");

        final String field = terraformTemplateDao.concatenateNestedField(TerraformTemplate.Fields.TASKS_IN_PROGRESS, taskId);
        terraformTemplateDao.updateTemplateUnset(templateId, Collections.singleton(field));
    }

    @Override
    public void pollQueuedTask(final String templateId) {
        Asserts.notBlank(templateId, "templateId");
        terraformTemplateDao.pollQueuedTask(templateId);
    }

    @Override
    public Collection<TerraformTemplate> findTemplatesWithQueuedTasks() {
        return terraformTemplateDao.findTemplatesWithQueuedTasks();
    }

    @Override
    public Optional<TerraformTemplate> findSystemTemplateByName(final String templateName) {
        Asserts.notBlank(templateName, "templateName");
        return terraformTemplateDao.findSystemTemplateByName(templateName);
    }

    @Override
    public void updateTemplate(final String templateId, final TerraformTemplateDbUpdateParameters updateParameters) {
        Asserts.notBlank(templateId, "templateId");
        Asserts.notNull(updateParameters, "updateParameters");
        terraformTemplateDao.updateTemplate(templateId, updateParameters);
    }
}
