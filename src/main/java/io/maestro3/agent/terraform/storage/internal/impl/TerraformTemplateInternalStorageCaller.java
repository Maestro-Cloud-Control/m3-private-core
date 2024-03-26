package io.maestro3.agent.terraform.storage.internal.impl;

import io.maestro3.agent.integration.IFileManager;
import io.maestro3.agent.terraform.exception.UpdateException;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.storage.internal.ITerraformTemplateInternalStorageCaller;
import io.maestro3.agent.terraform.util.TerraformInternalStoragePathUtils;
import io.maestro3.agent.tf.integration.TerraformPipeLineContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.integration.TerraformPipeLineContext;

import java.io.File;
import java.util.Arrays;

@Service
public class TerraformTemplateInternalStorageCaller implements ITerraformTemplateInternalStorageCaller {

    private final IFileManager fileManager;
    private final String terraformBucketName;

    @Autowired
    public TerraformTemplateInternalStorageCaller(IFileManager fileManager,
                                                  @Value("${terraform.template.bucket}") String terraformBucketName) {
        this.fileManager = fileManager;
        this.terraformBucketName = terraformBucketName;
    }

    @Override
    public void uploadTemplate(final TerraformTemplate terraformTemplate,
                               final String templateContent) {
        final String path = TerraformInternalStoragePathUtils.getBaseTemplateFilePath(terraformTemplate);
        fileManager.uploadFile(terraformBucketName, path, templateContent);
    }

    @Override
    public void updateTemplate(final TerraformTemplate terraformTemplate, final String newContent) {
        final String path = TerraformInternalStoragePathUtils.getBaseTemplateFilePath(terraformTemplate);
        final byte[] existingContent = fileManager.getFileContent(terraformBucketName, path);
        if (Arrays.equals(existingContent, newContent.getBytes())) {
            throw new UpdateException(UpdateException.Reason.NOTHING_TO_UPDATE);
        }

        fileManager.uploadFile(terraformBucketName, path, newContent);
    }

    @Override
    public void deleteTemplate(final TerraformTemplate terraformTemplate) {
        final String directoryPath = TerraformInternalStoragePathUtils.getRootTemplateDirectoryPath(
                terraformTemplate.getTenantName(), terraformTemplate.getName());
        fileManager.deleteDirectory(terraformBucketName, directoryPath);
    }

    @Override
    public void downloadTemplate(final TerraformPipeLineContext context, final File targetDirectory) {
        final String directoryPath = TerraformInternalStoragePathUtils.getBaseTemplateDirectoryPath(context);
        fileManager.downloadDirectory(terraformBucketName, directoryPath, targetDirectory);
    }

    @Override
    public void downloadStackTemplateCopy(final TerraformPipeLineContext context, final File targetDirectory) {
        final String directoryPath = TerraformInternalStoragePathUtils.getTemplateDirectoryPath(context);
        fileManager.downloadDirectory(terraformBucketName, directoryPath, targetDirectory);
    }
}
