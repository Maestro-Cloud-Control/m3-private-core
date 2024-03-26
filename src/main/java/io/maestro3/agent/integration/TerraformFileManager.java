package io.maestro3.agent.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.integration.TerraformPipeLineContext;
import team.syndicate.terraform.integration.manager.ITerraformFileManager;
import team.syndicate.terraform.management.extension.util.TerraformInternalStoragePathUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class TerraformFileManager implements ITerraformFileManager {

    private static final Logger LOG = LoggerFactory.getLogger(TerraformFileManager.class);

    private final IFileManager fileManager;
    private final String terraformBucketName;

    @Autowired
    public TerraformFileManager(IFileManager fileManager,
                                @Value("${terraform.template.bucket}") String terraformBucketName) {
        this.fileManager = fileManager;
        this.terraformBucketName = terraformBucketName;
    }

    @Override
    public void uploadTemplateDirectory(final TerraformPipeLineContext context, final File executionDirectory) {
        final String templateDirectoryPath = TerraformInternalStoragePathUtils.getTemplateDirectoryPath(context);
        if (context.isStackReuse()) {
            fileManager.deleteDirectory(terraformBucketName, templateDirectoryPath);
        }
        fileManager.uploadDirectory(terraformBucketName, templateDirectoryPath, executionDirectory);
    }

    @Override
    public void saveExecutionFile(final TerraformPipeLineContext context,
                                  final String fileName,
                                  final File executionDirectory) {
        final File file = new File(String.join(File.separator, executionDirectory.getAbsolutePath()), fileName);
        if (!file.exists()) {
            return;
        }

        try {
            final byte[] fileContent = Files.readAllBytes(file.toPath());
            final String executionFilePath = TerraformInternalStoragePathUtils.getExecutionFilePath(context, fileName);
            fileManager.uploadFile(terraformBucketName, executionFilePath, fileContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void downloadExecutionFile(final TerraformPipeLineContext context,
                                      final String fileName,
                                      final File executionDirectory) {
        final String executionFilePath = TerraformInternalStoragePathUtils.getExecutionFilePath(context, fileName);
        final byte[] data = fileManager.getFileContent(terraformBucketName, executionFilePath);
        final Path resultFilePath = Paths.get(executionDirectory.getAbsolutePath(), fileName);
        try {
            Files.write(resultFilePath, data, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void copyExecutionFile(final TerraformPipeLineContext context, final String fileName) {
        final String sourceExecutionFilePath = TerraformInternalStoragePathUtils.getBaseExecutionFilePath(context, fileName);
        final String destinationExecutionFilePath = TerraformInternalStoragePathUtils.getExecutionFilePath(context, fileName);
        LOG.info("Copy from {} to {}", sourceExecutionFilePath, destinationExecutionFilePath);
        fileManager.copyFile(terraformBucketName, sourceExecutionFilePath, terraformBucketName, destinationExecutionFilePath);
    }

    @Override
    public void saveExecutionLog(final TerraformPipeLineContext context, final String command, final String log) {
        final String logPath = TerraformInternalStoragePathUtils.getLogFilePath(context, command);
        fileManager.uploadFile(terraformBucketName, logPath, log.getBytes());
    }

    @Override
    public void saveResourcesInfo(final TerraformPipeLineContext context, final String resourcesInfo) {
        final String resourcesPath = TerraformInternalStoragePathUtils.getResourcesFilePath(context);
        fileManager.uploadFile(terraformBucketName, resourcesPath, resourcesInfo.getBytes());
    }

    @Override
    public void clearStackDirectory(final TerraformPipeLineContext context) {
        final String directoryPath = TerraformInternalStoragePathUtils.getDirectoryPath(context);
        fileManager.deleteDirectory(terraformBucketName, directoryPath);
    }
}
