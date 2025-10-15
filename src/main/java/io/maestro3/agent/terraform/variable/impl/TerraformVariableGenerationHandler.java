package io.maestro3.agent.terraform.variable.impl;

import io.maestro3.agent.terraform.model.PlatformServiceVariable;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.agent.terraform.variable.ITerraformVariableGenerationHandler;
import io.maestro3.agent.terraform.variable.ServerVariableResolvingContext;
import io.maestro3.agent.terraform.variable.resolver.IServerContextVariableResolver;
import io.maestro3.agent.terraform.variable.resolver.IServerGenerativeVariablePrefixHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformTemplateVariable;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformVariableType;
import team.syndicate.terraform.engine.terraform.variable.context.IConditionalContextVariableResolver;
import team.syndicate.terraform.engine.terraform.variable.context.IContextVariableResolver;
import team.syndicate.terraform.engine.terraform.variable.prefix.IGenerativeTerraformVariablePrefixHandler;
import team.syndicate.terraform.engine.utils.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TerraformVariableGenerationHandler implements ITerraformVariableGenerationHandler {

    private final Map<String, IContextVariableResolver> contextVariableResolverMap;
    private final Map<String, IServerContextVariableResolver> serverContextVariableResolverMap;
    private final Collection<IGenerativeTerraformVariablePrefixHandler> variablePrefixHandlers;
    private final Collection<IServerGenerativeVariablePrefixHandler> serverVariablePrefixHandlers;

    @Autowired
    public TerraformVariableGenerationHandler(List<IContextVariableResolver> contextVariableResolvers,
                                              List<IGenerativeTerraformVariablePrefixHandler> variablePrefixHandlers) {
        contextVariableResolverMap = contextVariableResolvers.stream()
                .collect(Collectors.toMap(IContextVariableResolver::getVariableName, Function.identity()));
        serverContextVariableResolverMap = contextVariableResolvers.stream()
                .filter(IServerContextVariableResolver.class::isInstance)
                .map(IServerContextVariableResolver.class::cast)
                .collect(Collectors.toMap(IServerContextVariableResolver::getVariableName, Function.identity()));
        this.variablePrefixHandlers = variablePrefixHandlers;
        this.serverVariablePrefixHandlers = variablePrefixHandlers.stream()
                .filter(IServerGenerativeVariablePrefixHandler.class::isInstance)
                .map(IServerGenerativeVariablePrefixHandler.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public boolean accept(final PlatformServiceVariable serviceVariable, final boolean acceptanceCondition) {
        if (acceptAsContextVariable(serviceVariable, acceptanceCondition)) {
            return true;
        }
        return acceptAsGenerativePrefixVariable(serviceVariable);
    }

    private boolean acceptAsContextVariable(final PlatformServiceVariable serviceVariable, final boolean acceptanceCondition) {
        final IContextVariableResolver resolver = contextVariableResolverMap.get(serviceVariable.getName());
        if (resolver == null) {
            return false;
        }

        final TerraformVariableType terraformVariableType = TerraformVariableType.valueOf(serviceVariable.getType().name());
        if (resolver instanceof IConditionalContextVariableResolver) {
            IConditionalContextVariableResolver conditionalResolver = (IConditionalContextVariableResolver) resolver;
            return conditionalResolver.accept(serviceVariable.getName(), terraformVariableType, serviceVariable.getDefaultValue(),
                    serviceVariable.getValue(), acceptanceCondition);
        }
        return resolver.accept(serviceVariable.getName(), terraformVariableType, serviceVariable.getDefaultValue(),
                serviceVariable.getValue());
    }

    private boolean acceptAsGenerativePrefixVariable(final PlatformServiceVariable serviceVariable) {
        final TerraformVariableType terraformVariableType = TerraformVariableType.valueOf(serviceVariable.getType().name());
        return variablePrefixHandlers.stream()
                .anyMatch(handler -> handler.accept(serviceVariable.getName(), terraformVariableType,
                        serviceVariable.getDefaultValue(), serviceVariable.getValue()));
    }

    @Override
    public Optional<Object> resolveForServer(final PlatformServiceVariable serviceVariable,
                                             final TerraformUserVariable userVariable,
                                             final ServerVariableResolvingContext context) {
        final String variableName = serviceVariable.getName();
        final TerraformVariableType variableType = TerraformVariableType.valueOf(serviceVariable.getType().name());
        final Object value = Optional.ofNullable(userVariable).map(TerraformUserVariable::getValue).orElse(serviceVariable.getValue());
        return resolveForServer(variableName, variableType, serviceVariable.getDefaultValue(), value, context);
    }

    @Override
    public Map<String, TerraformUserVariable> fillServerVariables(final TerraformTemplate template,
                                                                  final Map<String, TerraformUserVariable> requestVariables,
                                                                  final ServerVariableResolvingContext context) {
        final Map<String, TerraformTemplateVariable> templateVariables = template.getTemplateVariables();
        if (CollectionUtils.isEmpty(templateVariables)) {
            return requestVariables;
        }

        final Map<String, TerraformUserVariable> predefinedVariables = template.getVariables();
        final Map<String, TerraformUserVariable> serverContextVariables = new HashMap<>();
        for (final TerraformTemplateVariable templateVariable : templateVariables.values()) {
            final String variableName = templateVariable.getName();
            final Optional<Object> contextVariable = resolveForServer(templateVariable, predefinedVariables.get(variableName),
                    requestVariables.get(variableName), context);
            if (contextVariable.isEmpty()) {
                continue;
            }
            final TerraformUserVariable resolvedVariable = new TerraformUserVariable()
                    .withName(variableName)
                    .withType(templateVariable.getType())
                    .withValue(contextVariable.get());
            serverContextVariables.put(variableName, resolvedVariable);
        }
        if (CollectionUtils.isEmpty(serverContextVariables)) {
            return requestVariables;
        }
        final Map<String, TerraformUserVariable> resultVariables = new HashMap<>(requestVariables);
        resultVariables.putAll(serverContextVariables);
        return resultVariables;
    }

    private Optional<Object> resolveForServer(final TerraformTemplateVariable templateVariable,
                                              final TerraformUserVariable predefinedVariable,
                                              final TerraformUserVariable userVariable,
                                              final ServerVariableResolvingContext context) {
        final String variableName = templateVariable.getName();
        final Object defaultValue = templateVariable.isSecured() ? Optional.empty() : templateVariable.getDefaultValue();
        final Object value;
        if (userVariable != null) {
            value = userVariable.getValue();
        } else if (predefinedVariable != null) {
            if (predefinedVariable.isSecured()) {
                value = Optional.empty();
            } else {
                value = predefinedVariable.getValue();
            }
        } else {
            value = null;
        }

        return resolveForServer(variableName, templateVariable.getType(), defaultValue, value, context);
    }

    private Optional<Object> resolveForServer(final String variableName,
                                              final TerraformVariableType variableType,
                                              final Object defaultValue,
                                              final Object value,
                                              final ServerVariableResolvingContext context) {
        final IServerContextVariableResolver serverResolver = serverContextVariableResolverMap.get(variableName);
        if (serverResolver == null) {
            final Optional<IServerGenerativeVariablePrefixHandler> prefixHandler = serverVariablePrefixHandlers.stream()
                    .filter(handler -> handler.accept(variableName))
                    .findFirst();
            if (prefixHandler.isEmpty()) {
                return Optional.empty();
            }

            final IServerGenerativeVariablePrefixHandler handler = prefixHandler.get();
            if (handler.accept(variableName, variableType, defaultValue, value)) {
                return Optional.ofNullable(handler.generateForServer(context));
            }
            return Optional.empty();
        }

        if (serverResolver.accept(variableName, variableType, defaultValue, value)) {
            return Optional.ofNullable(serverResolver.resolveForServer(context));
        }
        return Optional.empty();
    }
}
