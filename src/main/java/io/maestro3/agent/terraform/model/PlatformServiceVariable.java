package io.maestro3.agent.terraform.model;

import io.maestro3.sdk.v3.model.terraform.template.VariableType;

public class PlatformServiceVariable {
    private final String name;
    private final VariableType type;
    private final Object defaultValue;
    private final Object value;
    private final String description;
    private final boolean sensitive;

    public PlatformServiceVariable(String name, VariableType type, Object defaultValue, Object value, String description, boolean sensitive) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.value = value;
        this.description = description;
        this.sensitive = sensitive;
    }

    public String getName() {
        return name;
    }

    public VariableType getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Object getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSensitive() {
        return sensitive;
    }
}
