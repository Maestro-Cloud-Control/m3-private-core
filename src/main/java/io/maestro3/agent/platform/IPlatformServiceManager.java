package io.maestro3.agent.platform;

import io.maestro3.agent.platform.model.PlatformServiceDefinitionInfo;
import io.maestro3.agent.platform.model.PlatformServiceEntryInfo;
import io.maestro3.agent.platform.model.params.ActivatePlatformServiceParams;
import io.maestro3.agent.platform.model.params.DeactivatePlatformServiceParams;
import io.maestro3.agent.platform.model.params.ListPlatformServiceDefinitionsParams;
import io.maestro3.agent.platform.model.params.ListPlatformServiceEntriesParams;
import io.maestro3.agent.platform.model.params.RegisterPlatformServiceParams;
import io.maestro3.agent.terraform.model.PlatformServiceVariable;

import java.util.Collection;

public interface IPlatformServiceManager {
    String registerPlatformService(RegisterPlatformServiceParams params);

    PlatformServiceEntryInfo activateService(ActivatePlatformServiceParams params);

    Collection<PlatformServiceVariable> getServiceVariables(String serviceName);

    Collection<PlatformServiceEntryInfo> listServiceEntries(ListPlatformServiceEntriesParams params);

    Collection<PlatformServiceDefinitionInfo> listServiceDefinitions(ListPlatformServiceDefinitionsParams params);

    void deactivateService(DeactivatePlatformServiceParams params);

    String unregisterPlatformService(String serviceName);
}
