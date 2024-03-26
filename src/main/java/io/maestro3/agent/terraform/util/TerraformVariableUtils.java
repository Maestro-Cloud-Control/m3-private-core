package io.maestro3.agent.terraform.util;

import io.maestro3.agent.tf.integration.model.TerraformVariable;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.v3.model.terraform.TerraformTaskVariable;
import io.maestro3.sdk.v3.model.terraform.template.SdkTerraformTemplateVariable;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TerraformVariableUtils {

    private TerraformVariableUtils() {
        throw new UnsupportedOperationException("Class is not designed for an instantiation");
    }

    public static Map<String, TerraformVariable> convertRequestVariables(final Map<String, TerraformTaskVariable> requestVariables) {
        if (CollectionUtils.isEmpty(requestVariables)) {
            return Collections.emptyMap();
        }

        return requestVariables.values().stream()
                .map(taskVariable -> new TerraformVariable(taskVariable.getName(), taskVariable.getValue()))
                .collect(Collectors.toMap(TerraformVariable::getName, Function.identity()));
    }

    public static Map<String, TerraformVariable> convertSdkVariables(final Map<String, SdkTerraformTemplateVariable> requestVariables) {
        if (CollectionUtils.isEmpty(requestVariables)) {
            return Collections.emptyMap();
        }

        return requestVariables.values().stream()
                .map(taskVariable -> new TerraformVariable(taskVariable.getName(), taskVariable.getValue()))
                .collect(Collectors.toMap(TerraformVariable::getName, Function.identity()));
    }
}
