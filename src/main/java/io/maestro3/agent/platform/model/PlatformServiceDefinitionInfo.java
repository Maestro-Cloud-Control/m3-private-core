package io.maestro3.agent.platform.model;

import java.util.Collection;

public class PlatformServiceDefinitionInfo {
    private final String name;
    private final String productVersion;
    private final Collection<String> cloud;
    private final Collection<String> categories;
    private final String summaryDescription;
    private final String templateType;

    public PlatformServiceDefinitionInfo(String name, String productVersion, Collection<String> cloud,
                                         Collection<String> categories, String summaryDescription, String templateType) {
        this.name = name;
        this.productVersion = productVersion;
        this.cloud = cloud;
        this.categories = categories;
        this.summaryDescription = summaryDescription;
        this.templateType = templateType;
    }

    public String getName() {
        return name;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public Collection<String> getCloud() {
        return cloud;
    }

    public Collection<String> getCategories() {
        return categories;
    }

    public String getSummaryDescription() {
        return summaryDescription;
    }

    public String getTemplateType() {
        return templateType;
    }
}
