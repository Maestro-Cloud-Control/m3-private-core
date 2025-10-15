package io.maestro3.agent.terraform.variable.resolver;

import io.maestro3.agent.terraform.variable.ServerVariableResolvingContext;
import team.syndicate.terraform.engine.terraform.variable.prefix.IGenerativeTerraformVariablePrefixHandler;

public interface IServerGenerativeVariablePrefixHandler extends IGenerativeTerraformVariablePrefixHandler {

    Object generateForServer(ServerVariableResolvingContext context);
}
