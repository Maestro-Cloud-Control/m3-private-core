package io.maestro3.agent.terraform.variable.resolver.impl;

import io.maestro3.agent.terraform.variable.ServerVariableResolvingContext;
import io.maestro3.agent.terraform.variable.resolver.IServerContextVariableResolver;
import org.springframework.stereotype.Component;
import team.syndicate.terraform.engine.terraform.integration.task.ExecuteTerraformTaskRequest;
import team.syndicate.terraform.engine.terraform.variable.context.AbstractContextVariableResolver;

@Component
public class OwnerVariableResolver extends AbstractContextVariableResolver implements IServerContextVariableResolver {

    public static final String VARIABLE_NAME = "m3_owner";

    public OwnerVariableResolver() {
        super(VARIABLE_NAME);
    }

    @Override
    public Object resolve(final ExecuteTerraformTaskRequest task) {
        return task.getRequester();
    }

    @Override
    public Object resolveForServer(ServerVariableResolvingContext context) {
        return context.getRequester();
    }
}
