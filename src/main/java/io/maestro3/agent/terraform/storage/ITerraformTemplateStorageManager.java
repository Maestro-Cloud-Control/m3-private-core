package io.maestro3.agent.terraform.storage;

import io.maestro3.agent.model.base.BaseTenant;
import io.maestro3.agent.terraform.model.CreateOrUpdateTerraformTemplateParams;
import io.maestro3.agent.terraform.model.DeleteTerraformTemplateResult;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import team.syndicate.terraform.integration.TerraformPipeLineContext;
import team.syndicate.terraform.integration.model.TerraformTemplateStorageType;
import team.syndicate.terraform.integration.model.TerraformTemplateVariable;

import java.io.File;
import java.util.Collection;
import java.util.Map;

public interface ITerraformTemplateStorageManager {

    TerraformTemplate createTemplate(BaseTenant tenant, String owner, CreateOrUpdateTerraformTemplateParams params);

    TerraformTemplate updateTemplate(TerraformTemplate existingTemplate,
                                     String requester,
                                     CreateOrUpdateTerraformTemplateParams params);

    void planTemplate(TerraformTemplate template, String requester,
                      Map<String, TerraformTemplateVariable> requestVariables);

    void applyTemplate(TerraformTemplate template, String requester,
                       Map<String, TerraformTemplateVariable> requestVariables,
                       String paasUuid);

    void destroyTemplateStack(TerraformTemplate template, String stackId, String requester);

    DeleteTerraformTemplateResult deleteTemplate(TerraformTemplate template);

    File downloadTemplate(TerraformPipeLineContext context, File executionDirectory);

    Collection<TerraformTemplateStorageType> getSupportedStorageTypes();
}
