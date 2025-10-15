package io.maestro3.agent.terraform.variable;

import io.maestro3.agent.terraform.model.PlatformServiceVariable;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;

import java.util.Map;
import java.util.Optional;

public interface ITerraformVariableGenerationHandler {
    boolean accept(PlatformServiceVariable serviceVariable, boolean acceptanceCondition);

    Optional<Object> resolveForServer(PlatformServiceVariable serviceVariable, TerraformUserVariable userVariable, ServerVariableResolvingContext context);

    Map<String, TerraformUserVariable> fillServerVariables(TerraformTemplate template,
                                                           Map<String, TerraformUserVariable> requestVariables,
                                                           ServerVariableResolvingContext context);
}
