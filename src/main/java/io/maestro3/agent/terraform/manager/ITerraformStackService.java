package io.maestro3.agent.terraform.manager;

import io.maestro3.agent.terraform.model.TerraformStack;
import io.maestro3.agent.terraform.model.TerraformTemplateDto;
import team.syndicate.terraform.integration.model.TemplateStatus;
import team.syndicate.terraform.integration.model.TerraformTemplateVariable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ITerraformStackService {

    TerraformStack createStack(TerraformTemplateDto terraformTemplateDto);

    Optional<TerraformStack> getSingleStack(String templateId);

    Optional<TerraformStack> getStack(String stackId);

    Collection<TerraformStack> findByTemplateName(String tenantName, String templateName);

    Optional<TerraformStack> findByTemplateName(String tenantName, String templateName, String stackId);

    void updateStackStatus(TerraformStack stack, TemplateStatus newStatus);

    void updateStackStatus(String stackId, TemplateStatus newStatus);

    void updateStackVariables(String stackId, Map<String, TerraformTemplateVariable> newVariables);

    void updateStackProviders(String stackId, Set<String> newProviders);

    void updateStackSubDirectory(String stackId, String newSubDirectory);

    Collection<TerraformStack> findByTenant(String tenantName, Collection<String> stackIds);

    void delete(String stackId);

    Optional<TerraformStack> findByEntryId(String entryId);
}
