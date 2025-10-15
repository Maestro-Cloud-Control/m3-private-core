package io.maestro3.agent.platform.deployment;

import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.platform.model.PlatformServiceDefinition;
import io.maestro3.agent.platform.model.PlatformServiceDefinitionInfo;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentInfo;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import io.maestro3.agent.platform.model.PlatformServiceEntryInfo;
import io.maestro3.agent.terraform.model.PlatformServiceVariable;
import team.syndicate.terraform.engine.terraform.integration.model.TerraformUserVariable;

import java.util.Collection;
import java.util.Map;

public interface IPlatformServiceDeploymentManager {

    PlatformServiceDeploymentInfo handleServiceRegistration(String serviceName, Map<String, String> deploymentParams);

    String buildRegistrationMessage(PlatformServiceDefinition platformServiceDefinition);

    PlatformServiceEntry activateService(ITenant tenant, IRegion region,
                                         Map<String, String> providerParams,
                                         Map<String, TerraformUserVariable> userVariables,
                                         String requester, String serviceName);

    PlatformServiceEntryInfo convertEntryInfo(PlatformServiceEntry platformServiceEntry);

    Collection<PlatformServiceVariable> getServiceVariables(Map<String, String> providerParams);

    Collection<PlatformServiceEntryInfo> getServiceEntryInfos(Collection<PlatformServiceEntry> serviceEntries);

    Collection<PlatformServiceDefinitionInfo> getServiceDefinitionInfos(Collection<PlatformServiceDefinition> serviceDefinitions);

    void deactivateService(PlatformServiceEntry serviceEntry, String requester);

    void unregisterService(PlatformServiceDefinition serviceDefinition, boolean serviceEntryExists);

    PlatformServiceDeploymentType getDeploymentType();
}
