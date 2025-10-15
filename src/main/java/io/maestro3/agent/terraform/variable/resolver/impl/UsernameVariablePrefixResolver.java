package io.maestro3.agent.terraform.variable.resolver.impl;

import io.maestro3.agent.terraform.variable.ServerVariableResolvingContext;
import io.maestro3.agent.terraform.variable.resolver.IServerGenerativeVariablePrefixHandler;
import org.springframework.stereotype.Component;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformVariableType;
import team.syndicate.terraform.engine.terraform.variable.prefix.AbstractTerraformVariablePrefixHandler;

import java.util.Objects;

@Component
public class UsernameVariablePrefixResolver extends AbstractTerraformVariablePrefixHandler
        implements IServerGenerativeVariablePrefixHandler {

    private static final String FLAG = "u";

    public UsernameVariablePrefixResolver() {
        super(FLAG);
    }

    @Override
    public Object generateForServer(final ServerVariableResolvingContext context) {
        return context.getRequester();
    }

    @Override
    public boolean accept(final String variableName, final TerraformVariableType type, final Object defaultValue, final Object value) {
        if (!accept(variableName)) {
            return false;
        }
        if (TerraformVariableType.STRING != type) {
            return false;
        }
        if (Objects.nonNull(defaultValue)) {
            return false;
        }
        return Objects.isNull(value);
    }

    @Override
    public Object generateValue() {
        throw new UnsupportedOperationException();
    }
}
