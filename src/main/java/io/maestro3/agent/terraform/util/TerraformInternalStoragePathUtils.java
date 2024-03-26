package io.maestro3.agent.terraform.util;

import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.sdk.internal.util.StringUtils;
import team.syndicate.terraform.integration.TerraformPipeLineContext;
import team.syndicate.terraform.integration.model.ITerraformStackInfo;
import team.syndicate.terraform.integration.model.ITerraformTemplateInfo;

import java.util.Optional;

public final class TerraformInternalStoragePathUtils {

    private static final String DELIMITER = "/";
    private static final String BASE_DIR_NAME = "base";
    private static final String TEMPLATE_DIR_NAME = "template";
    private static final String EXECUTION_FILES_DIR_NAME = "execution-files";
    private static final String LOG_DIR_NAME = "logs";
    private static final String RESOURCE_DIR_NAME = "resources";
    private static final String LOG_FILE_NAME_PATTERN = "%s.txt";
    private static final String RESOURCES_FILE_NAME_PATTERN = "%s_resources.txt";

    private TerraformInternalStoragePathUtils() {
        throw new UnsupportedOperationException("Class is not designed for an instantiation");
    }

    public static String getBaseTemplateFilePath(final TerraformTemplate terraformTemplate) {
        return String.join(DELIMITER, getBaseSubDirectoryPath(terraformTemplate, TEMPLATE_DIR_NAME),
                terraformTemplate.getTemplateFileName());
    }

    public static String getBaseTemplateDirectoryPath(final TerraformPipeLineContext context) {
        return getBaseSubDirectoryPath(context, TEMPLATE_DIR_NAME);
    }

    public static String getTemplateDirectoryPath(final TerraformPipeLineContext context) {
        return getSubDirectoryPath(context, TEMPLATE_DIR_NAME);
    }

    public static String getBaseExecutionFilePath(final TerraformPipeLineContext context, final String fileName) {
        String baseSubDirectoryPath = getBaseSubDirectoryPath(context, EXECUTION_FILES_DIR_NAME);
        return String.join(DELIMITER, baseSubDirectoryPath, fileName);
    }

    public static String getExecutionFilePath(final TerraformPipeLineContext context, final String fileName) {
        final String subDirectoryPath = getSubDirectoryPath(context, EXECUTION_FILES_DIR_NAME);
        return String.join(DELIMITER, subDirectoryPath, fileName);
    }

    public static String getResourcesFilePath(final TerraformPipeLineContext context) {
        final String resourcesSubDirectoryPath = getSubDirectoryPath(context, RESOURCE_DIR_NAME);
        final String resourcesFileName = String.format(RESOURCES_FILE_NAME_PATTERN, context.getTemplateName());
        return String.join(DELIMITER, resourcesSubDirectoryPath, resourcesFileName);
    }

    public static String getLogFilePath(final TerraformPipeLineContext context, final String command) {
        final String logSubDirectoryPath = getSubDirectoryPath(context, LOG_DIR_NAME);
        final String logFileName = String.format(LOG_FILE_NAME_PATTERN, command.toLowerCase());
        return String.join(DELIMITER, logSubDirectoryPath, command, logFileName);
    }

    private static String getSubDirectoryPath(final TerraformPipeLineContext context, final String subDirectory) {
        return String.join(DELIMITER, getDirectoryPath(context), subDirectory);
    }

    private static String getBaseSubDirectoryPath(final TerraformPipeLineContext context, final String subDirectory) {
        return String.join(DELIMITER, getBaseDirectoryPath(context), subDirectory);
    }

    private static String getBaseSubDirectoryPath(final TerraformTemplate terraformTemplate, final String subDirectory) {
        return String.join(DELIMITER, getBaseDirectoryPath(terraformTemplate), subDirectory);
    }

    private static String getBaseDirectoryPath(final TerraformPipeLineContext context) {
        final String tenantName = context.getTenantName();
        final ITerraformTemplateInfo template = context.getTemplate();
        return getDirectoryPath(tenantName, template, null);
    }

    private static String getBaseDirectoryPath(final TerraformTemplate terraformTemplate) {
        return getDirectoryPath(terraformTemplate.getTenantName(), terraformTemplate.getName(), null);
    }

    public static String getDirectoryPath(final TerraformPipeLineContext context) {
        final String tenantName = context.getTenantName();
        final ITerraformTemplateInfo template = context.getTemplate();
        final ITerraformStackInfo stack = context.getStack();
        return getDirectoryPath(tenantName, template, stack);
    }

    private static String getDirectoryPath(final String tenantName,
                                           final ITerraformTemplateInfo template,
                                           final ITerraformStackInfo stack) {
        return getDirectoryPath(tenantName, template.getName(),
                Optional.ofNullable(stack).map(ITerraformStackInfo::getStackId).orElse(BASE_DIR_NAME));
    }

    private static String getDirectoryPath(final String tenantName,
                                           final String templateName,
                                           final String stackId) {
        return String.join(DELIMITER, getRootTemplateDirectoryPath(tenantName, templateName), getStackPathPart(stackId));
    }

    public static String getRootTemplateDirectoryPath(final String tenantName,
                                                      final String templateName) {
        return String.join(DELIMITER, tenantName, templateName);
    }

    private static String getStackPathPart(final String stackId) {
        if (StringUtils.isBlank(stackId)) {
            return BASE_DIR_NAME;
        }
        return stackId;
    }
}
