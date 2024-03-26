package io.maestro3.agent.terraform.storage.internal;

import io.maestro3.agent.terraform.model.TerraformTemplate;
import team.syndicate.terraform.integration.TerraformPipeLineContext;

import java.io.File;

public interface ITerraformTemplateInternalStorageCaller {

    void uploadTemplate(TerraformTemplate terraformTemplate, String templateContent);

    void updateTemplate(TerraformTemplate terraformTemplate, String newContent);

    void deleteTemplate(TerraformTemplate terraformTemplate);

    void downloadTemplate(TerraformPipeLineContext context, File executionDirectory);

    void downloadStackTemplateCopy(TerraformPipeLineContext context, File targetDirectory);

}
