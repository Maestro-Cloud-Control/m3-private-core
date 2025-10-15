package io.maestro3.agent.terraform.sevice.impl;

import io.maestro3.agent.dao.ITerraformStackDao;
import io.maestro3.agent.terraform.sevice.ITerraformStackService;
import io.maestro3.agent.terraform.model.TerraformStack;
import io.maestro3.agent.terraform.model.TerraformTemplateDto;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateStorageInfo;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;
import team.syndicate.terraform.engine.utils.Asserts;

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
                .withDescription(terraformTemplateDto.getDescription())
                .withOwner(terraformTemplateDto.getOwner())
                .withProviders(terraformTemplateDto.getProviders())
                .withSubDirectory(getStackSubDirectory(terraformTemplateDto.getStorageInfo()))
                .withServiceEntryId(terraformTemplateDto.getServiceEntryId());
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
    public Optional<TerraformStack> findByServiceEntry(final PlatformServiceEntry serviceEntry) {
        Asserts.notNull(serviceEntry, "serviceEntry");
        return stackDao.findByServiceEntryId(serviceEntry.getServiceEntryId());
    }

    @Override
    public void updateStackStatus(String stackId, TemplateStatus newStatus) {
        Asserts.notBlank(stackId, "stackId");
        Asserts.notNull(newStatus, "newStatus");

        stackDao.updateStack(stackId, Collections.singletonMap(TerraformStack.Fields.STATUS, newStatus));
    }

    @Override
    public void updateStackVariables(String stackId, Map<String, TerraformUserVariable> newVariables) {
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
