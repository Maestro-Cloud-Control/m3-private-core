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

import io.maestro3.agent.dao.ITerraformStackDao;
import io.maestro3.agent.terraform.manager.ITerraformStackService;
import io.maestro3.agent.terraform.model.TerraformStack;
import io.maestro3.agent.terraform.model.TerraformTemplateDto;
import io.maestro3.agent.tf.integration.model.TemplateStatus;
import io.maestro3.agent.tf.integration.model.TerraformTemplateStorageInfo;
import io.maestro3.agent.tf.integration.model.TerraformVariable;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class TerraformStackService implements ITerraformStackService {

    private final ITerraformStackDao stackDao;

    @Autowired
    public TerraformStackService(ITerraformStackDao stackDao) {
        this.stackDao = stackDao;
    }

    @Override
    public TerraformStack createStack(TerraformTemplateDto terraformTemplateDto) {
        Asserts.notNull(terraformTemplateDto, "terraformTemplate");
        final TerraformStack stack = new TerraformStack()
                .withCloud(terraformTemplateDto.getCloud())
                .withTenantName(terraformTemplateDto.getTenantName())
                .withTemplateId(terraformTemplateDto.getTemplateId())
                .withTemplateName(terraformTemplateDto.getName())
                .withStatus(TemplateStatus.PENDING_PLANNING)
                .withPaasUuid(terraformTemplateDto.getPaasUuid())
                .withDescription(terraformTemplateDto.getDescription())
                .withOwner(terraformTemplateDto.getOwner())
                .withProviders(terraformTemplateDto.getProviders())
                .withSubDirectory(getStackSubDirectory(terraformTemplateDto.getStorageInfo()));
        stackDao.save(stack);
        return stack;
    }

    private String getStackSubDirectory(TerraformTemplateStorageInfo storageInfo) {
        return Optional.ofNullable(storageInfo)
                .filter(info -> info.getStorageType().isGitBased())
                .map(TerraformTemplateStorageInfo::getStorageParams)
                .map(params -> params.get(TerraformTemplateStorageInfo.Fields.GIT_SUB_DIRECTORY))
                .orElse(null);
    }

    @Override
    public Optional<TerraformStack> getSingleStack(String templateId) {
        Asserts.notBlank(templateId, "templateId");

        return stackDao.findByTemplateId(templateId);
    }

    @Override
    public Optional<TerraformStack> getStack(String stackId) {
        Asserts.notBlank(stackId, "stackId");
        return stackDao.findByStackId(stackId);
    }

    @Override
    public Optional<TerraformStack> findByEntryId(String entryId) {
        Asserts.notBlank(entryId, "entryId");
        return stackDao.findByEntryID(entryId);
    }

    @Override
    public Collection<TerraformStack> findByTemplateName(String tenantName, String templateName) {
        Asserts.notBlank(tenantName, "tenantName");
        Asserts.notBlank(templateName, "templateName");
        return stackDao.findByTemplateName(tenantName, templateName);
    }

    @Override
    public Optional<TerraformStack> findByTemplateName(String tenantName, String templateName, String stackId) {
        Asserts.notBlank(tenantName, "tenantName");
        Asserts.notBlank(templateName, "templateName");
        Asserts.notBlank(stackId, "stackId");
        return stackDao.findByTemplateName(tenantName, templateName, stackId);
    }

    @Override
    public void updateStackStatus(TerraformStack stack, TemplateStatus newStatus) {
        Asserts.notNull(stack, "stack");
        Asserts.notNull(newStatus, "newStatus");

        if (stack.getStatus() == newStatus) {
            return;
        }

        updateStackStatus(stack.getStackId(), newStatus);
        stack.withStatus(newStatus);
    }

    @Override
    public void updateStackStatus(String stackId, TemplateStatus newStatus) {
        Asserts.notBlank(stackId, "stackId");
        Asserts.notNull(newStatus, "newStatus");

        stackDao.updateStack(stackId, Collections.singletonMap(TerraformStack.Fields.STATUS, newStatus));
    }

    @Override
    public void updateStackVariables(String stackId, Map<String, TerraformVariable> newVariables) {
        Asserts.notBlank(stackId, "stackId");
        stackDao.updateStack(stackId, Collections.singletonMap(TerraformStack.Fields.VARIABLES, newVariables));
    }

    @Override
    public void updateStackProviders(String stackId, Set<String> newProviders) {
        Asserts.notBlank(stackId, "stackId");
        stackDao.updateStack(stackId, Collections.singletonMap(TerraformStack.Fields.PROVIDERS, newProviders));
    }

    @Override
    public void updateStackSubDirectory(String stackId, String newSubDirectory) {
        Asserts.notBlank(stackId, "stackId");
        stackDao.updateStack(stackId, Collections.singletonMap(TerraformStack.Fields.SUB_DIRECTORY, newSubDirectory));
    }

    @Override
    public Collection<TerraformStack> findByTenant(String tenantName, Collection<String> stackIds) {
        Asserts.notBlank(tenantName, "tenantName");
        return stackDao.findByTenant(tenantName, stackIds);
    }

    @Override
    public void delete(String stackId) {
        Asserts.notBlank(stackId, "stackId");
        stackDao.delete(stackId);
    }
}
