package io.maestro3.agent.terraform.variable.resolver;

import io.maestro3.agent.terraform.variable.ServerVariableResolvingContext;
import team.syndicate.terraform.engine.terraform.variable.context.IContextVariableResolver;

public interface IServerContextVariableResolver extends IContextVariableResolver {

    Object resolveForServer(ServerVariableResolvingContext context);
}
