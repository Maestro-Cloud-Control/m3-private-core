package io.maestro3.agent.terraform.util;

import io.maestro3.sdk.v3.model.terraform.TerraformTaskVariable;
import io.maestro3.sdk.v3.model.terraform.template.VariableType;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformVariableType;
import team.syndicate.terraform.engine.utils.CollectionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TerraformVariableUtils {

    private TerraformVariableUtils() {
        throw new UnsupportedOperationException("Class is not designed for an instantiation");
    }

    public static Map<String, TerraformUserVariable> convertRequestVariables(final Map<String, TerraformTaskVariable> requestVariables) {
        if (CollectionUtils.isEmpty(requestVariables)) {
            return Collections.emptyMap();
        }

        return requestVariables.values().stream()
                .map(TerraformVariableUtils::convertRequestVariable)
                .collect(Collectors.toMap(TerraformUserVariable::getName, Function.identity()));
    }

    private static TerraformUserVariable convertRequestVariable(final TerraformTaskVariable requestVariable) {
        final TerraformVariableType type = TerraformVariableType.valueOf(requestVariable.getType().name());
        return new TerraformUserVariable()
                .withName(requestVariable.getName())
                .withType(type)
                .withValue(requestVariable.getValue());
    }

    public static VariableType convertVariableType(final TerraformVariableType terraformVariableType) {
        if (terraformVariableType == TerraformVariableType.BOOL) {
            return VariableType.BOOL;
        } else if (terraformVariableType == TerraformVariableType.NUMBER
                || terraformVariableType == TerraformVariableType.STRING) {
            return VariableType.STRING;
        } else if (terraformVariableType == TerraformVariableType.LIST
                || terraformVariableType == TerraformVariableType.SET
                || terraformVariableType == TerraformVariableType.TUPLE) {
            return VariableType.LIST;
        } else if (terraformVariableType == TerraformVariableType.MAP) {
            return VariableType.MAP;
        } else {
            return VariableType.COMPLEX;
        }
    }
}