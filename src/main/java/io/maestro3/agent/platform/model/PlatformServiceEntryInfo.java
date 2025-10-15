package io.maestro3.agent.platform.model;

public class PlatformServiceEntryInfo {
    private final String serviceName;
    private final String serviceId;
    private final PlatformServiceEntryState state;
    private final String templateName;
    private final String serviceUrl;

    public PlatformServiceEntryInfo(String serviceName, String serviceId, PlatformServiceEntryState state, String templateName, String serviceUrl) {
        this.serviceName = serviceName;
        this.serviceId = serviceId;
        this.state = state;
        this.templateName = templateName;
        this.serviceUrl = serviceUrl;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public PlatformServiceEntryState getState() {
        return state;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }
}
