package io.maestro3.agent.integration;

import io.maestro3.agent.terraform.storage.ITerraformTemplateStorageManager;
import io.maestro3.agent.terraform.storage.ITerraformTemplateStorageManagerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.integration.TerraformPipeLineContext;
import team.syndicate.terraform.integration.manager.ITerraformTemplateDownloader;
import team.syndicate.terraform.integration.model.ITerraformTemplateInfo;
import team.syndicate.terraform.integration.model.TerraformTemplateStorageInfo;
import team.syndicate.terraform.integration.model.TerraformTemplateStorageType;

import java.io.File;

@Service
public class TerraformTemplateDownloader implements ITerraformTemplateDownloader {

    private final ITerraformTemplateStorageManagerProvider storageManagerProvider;

    @Autowired
    public TerraformTemplateDownloader(ITerraformTemplateStorageManagerProvider storageManagerProvider) {
        this.storageManagerProvider = storageManagerProvider;
    }

    @Override
    public File downloadTemplate(TerraformPipeLineContext context, File targetDirectory) {
        ITerraformTemplateInfo template = context.getTemplate();
        TerraformTemplateStorageInfo storageInfo = template.getStorageInfo();
        TerraformTemplateStorageType storageType = storageInfo.getStorageType();
        ITerraformTemplateStorageManager storageManager = storageManagerProvider.provideStorageManager(storageType);
        return storageManager.downloadTemplate(context, targetDirectory);
    }
}
