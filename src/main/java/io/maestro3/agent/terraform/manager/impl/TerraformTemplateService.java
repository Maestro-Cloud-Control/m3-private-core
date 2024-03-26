/*
 * Copyright 2023 Maestro Cloud Control LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.maestro3.agent.terraform.manager.impl;

import io.maestro3.agent.dao.ITerraformTemplateDao;
import io.maestro3.agent.terraform.manager.ITerraformTemplateService;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.integration.task.TerraformTask;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

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
    public void updateTemplateStatus(TerraformTemplate template, TemplateStatus newStatus) {
        Asserts.notNull(template, "template");
        Asserts.notNull(newStatus, "newStatus");
        if (template.getStatus() == newStatus) {
            return;
        }

        updateTemplateStatus(template.getTemplateId(), newStatus);
        template.withStatus(newStatus);
    }

    @Override
    public void updateTemplateStatus(String templateId, TemplateStatus newStatus) {
        Asserts.notBlank(templateId, "templateId");
        Asserts.notNull(newStatus, "newStatus");

        terraformTemplateDao.updateTemplateSet(templateId,
                Collections.singletonMap(TerraformTemplate.Fields.STATUS, newStatus));
    }

    @Override
    public void updateTemplateProviders(String templateId, Set<String> newProviders) {
        Asserts.notBlank(templateId, "templateId");
        Asserts.notNull(newProviders, "newProviders");

        terraformTemplateDao.updateTemplateSet(templateId,
                Collections.singletonMap(TerraformTemplate.Fields.PROVIDERS, newProviders));
    }

    @Override
    public void addTaskInProgress(TerraformTemplate template, String taskId, TerraformTask task) {
        Asserts.notNull(template, "template");
        final String templateId = template.getTemplateId();
        Asserts.notBlank(templateId, "templateId");
        Asserts.notBlank(taskId, "taskId");
        Asserts.notNull(task, "task");

        if (template.isTaskInProgress(task)) {
            return;
        }

        final String field = String.join(".", TerraformTemplate.Fields.TASKS_IN_PROGRESS, taskId);
        terraformTemplateDao.updateTemplateSet(templateId, Collections.singletonMap(field, task));
        template.addTaskInProgress(taskId, task);
    }

    @Override
    public void removeTask(String templateId, String taskId) {
        Asserts.notBlank(templateId, "templateId");
        Asserts.notBlank(taskId, "taskId");

        final String field = String.join(".", TerraformTemplate.Fields.TASKS_IN_PROGRESS, taskId);
        terraformTemplateDao.updateTemplateUnset(templateId, Collections.singleton(field));
    }

    @Override
    public void addTaskInQueue(TerraformTemplate template, String task) {
        Asserts.notNull(template, "template");
        final String templateId = template.getTemplateId();
        Asserts.notBlank(templateId, "templateId");
        Asserts.notBlank(task, "task");

        if (template.isTaskInQueue(task)) {
            return;
        }

        terraformTemplateDao.updateTemplateAddToSet(templateId, TerraformTemplate.Fields.AUTO_TASK_QUEUE, task);
        template.addTaskToQueue(task);
    }

    @Override
    public String pollQueuedTask(TerraformTemplate template) {
        Asserts.notNull(template, "template");
        final String templateId = template.getTemplateId();
        Asserts.notBlank(templateId, "templateId");

        terraformTemplateDao.pollQueuedTask(templateId);
        return template.pollTaskFromQueue();
    }

    @Override
    public Collection<TerraformTemplate> findTemplatesWithQueuedTasks() {
        return terraformTemplateDao.findTemplatesWithQueuedTasks();
    }
}
