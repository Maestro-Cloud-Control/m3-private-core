package io.maestro3.agent.terraform.integration.context;

import team.syndicate.terraform.engine.management.util.PaasUtils;
import team.syndicate.terraform.engine.terraform.integration.IRegionContextExtension;

public class PaasTerraformPipeLineContextExtension implements IRegionContextExtension {

    private static final String PAAS_EXECUTION_DIRECTORY_PATH = "SYSTEM/PaaS/%s";

    private final String serviceName;
    private final String serviceEntryId;
    private final String taskId;
    private final String regionName;

    public PaasTerraformPipeLineContextExtension(String serviceName, String serviceEntryId, String taskId, String regionName) {
        this.serviceName = serviceName;
        this.serviceEntryId = serviceEntryId;
        this.taskId = taskId;
        this.regionName = regionName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceEntryId() {
        return serviceEntryId;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public String getExecutionDirectoryPath() {
        return String.format(PAAS_EXECUTION_DIRECTORY_PATH, serviceName);
    }

    @Override
    public String getFileStorageDirectoryPath() {
        return PaasUtils.getFileStoragePaasDirectory(serviceName);
    }

    @Override
    public String getRegionName() {
        return regionName;
    }
}
