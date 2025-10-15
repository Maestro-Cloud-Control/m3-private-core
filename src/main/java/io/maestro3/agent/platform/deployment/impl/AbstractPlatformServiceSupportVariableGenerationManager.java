package io.maestro3.agent.platform.deployment.impl;

import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.platform.deployment.IPlatformServiceDeploymentManager;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import io.maestro3.agent.terraform.model.PlatformServiceVariable;
import io.maestro3.agent.terraform.variable.ITerraformVariableGenerationHandler;
import io.maestro3.agent.terraform.variable.ServerVariableResolvingContext;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformVariableType;
import team.syndicate.terraform.engine.terraform.variable.prefix.IInputFilterVariablePrefixHandler;
import team.syndicate.terraform.engine.utils.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractPlatformServiceSupportVariableGenerationManager implements IPlatformServiceDeploymentManager {

    private final ITerraformVariableGenerationHandler variableGenerationHandler;
    private final Collection<IInputFilterVariablePrefixHandler> inputFilterHandlers;

    protected AbstractPlatformServiceSupportVariableGenerationManager(ITerraformVariableGenerationHandler variableGenerationHandler,
                                                                      List<IInputFilterVariablePrefixHandler> inputFilterHandlers) {
        this.variableGenerationHandler = variableGenerationHandler;
        this.inputFilterHandlers = inputFilterHandlers;
    }

    @Override
    public PlatformServiceEntry activateService(final ITenant tenant, final IRegion region,
                                                final Map<String, String> providerParams,
                                                final Map<String, TerraformUserVariable> userVariables,
                                                final String requester, final String serviceName) {
        final ServerVariableResolvingContext context = new ServerVariableResolvingContext(requester);
        final Collection<TerraformUserVariable> serverContextVariables =
                generateServerContextVariables(providerParams, userVariables, context);
        final Map<String, TerraformUserVariable> variables;
        if (CollectionUtils.isEmpty(serverContextVariables)) {
            variables = userVariables;
        } else {
            variables = new HashMap<>(userVariables);
            serverContextVariables.forEach(contextVar -> variables.put(contextVar.getName(), contextVar));
        }
        return handleServiceActivation(tenant, region, providerParams, variables, requester, serviceName);
    }

    private Collection<TerraformUserVariable> generateServerContextVariables(final Map<String, String> providerParams,
                                                                             final Map<String, TerraformUserVariable> userVariables,
                                                                             final ServerVariableResolvingContext context) {
        final Collection<PlatformServiceVariable> serviceVariables = getDeclaredServiceVariables(providerParams);
        if (CollectionUtils.isEmpty(serviceVariables)) {
            return Collections.emptySet();
        }

        return serviceVariables.stream()
                .map(serviceVariable -> generateServerContextVariable(serviceVariable, userVariables.get(serviceVariable.getName()), context))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private Optional<TerraformUserVariable> generateServerContextVariable(final PlatformServiceVariable serviceVariable,
                                                                          final TerraformUserVariable userVariable,
                                                                          final ServerVariableResolvingContext context) {
        final Optional<Object> value = variableGenerationHandler.resolveForServer(serviceVariable, userVariable, context);
        return value.map(val -> new TerraformUserVariable()
                .withName(serviceVariable.getName())
                .withType(TerraformVariableType.valueOf(serviceVariable.getType().name()))
                .withValue(val));
    }

    protected abstract PlatformServiceEntry handleServiceActivation(final ITenant tenant, final IRegion region,
                                                                    final Map<String, String> providerParams,
                                                                    final Map<String, TerraformUserVariable> userVariables,
                                                                    final String requester, final String serviceName);

    @Override
    public Collection<PlatformServiceVariable> getServiceVariables(final Map<String, String> providerParams) {
        final Collection<PlatformServiceVariable> serviceVariables = getDeclaredServiceVariables(providerParams);
        if (CollectionUtils.isEmpty(serviceVariables)) {
            return Collections.emptySet();
        }
        return serviceVariables.stream()
                .filter(this::acceptInput)
                .filter(variable -> !variableGenerationHandler.accept(variable, true))
                .collect(Collectors.toSet());
    }

    private boolean acceptInput(final PlatformServiceVariable serviceVariable) {
        final Collection<IInputFilterVariablePrefixHandler> acceptedHandlers = inputFilterHandlers.stream()
                .filter(handler -> handler.accept(serviceVariable.getName()))
                .collect(Collectors.toList());
        if (acceptedHandlers.isEmpty()) {
            return true;
        }

        return acceptedHandlers.stream().anyMatch(handler -> handler.acceptInput(serviceVariable.getName()));
    }

    protected abstract Collection<PlatformServiceVariable> getDeclaredServiceVariables(final Map<String, String> providerParams);
}
