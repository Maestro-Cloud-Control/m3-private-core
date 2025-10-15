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

package io.maestro3.agent.terraform.sevice;

import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.model.TerraformTemplateDbUpdateParameters;

import java.util.Collection;
import java.util.Optional;

public interface ITerraformTemplateService {
    boolean isTemplateExist(String name, String tenantName);

    void save(TerraformTemplate terraformTemplate);

    void delete(String templateId);

    Collection<TerraformTemplate> find(String tenantName, Collection<String> templateNames);

    Optional<TerraformTemplate> find(String tenantName, String templateName);

    Optional<TerraformTemplate> find(String templateId);

    void removeTask(String templateId, String taskId);

    void pollQueuedTask(String templateId);

    Collection<TerraformTemplate> findTemplatesWithQueuedTasks();

    Optional<TerraformTemplate> findSystemTemplateByName(String templateName);

    void updateTemplate(String templateId, TerraformTemplateDbUpdateParameters updateParameters);
}
