package io.maestro3.agent.terraform.sevice;

import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import io.maestro3.agent.platform.model.PlatformServiceEntryState;
import io.maestro3.agent.terraform.model.PlatformServiceTask;

import java.util.Collection;
import java.util.Optional;

public interface IPlatformServiceEntryService {
    void save(PlatformServiceEntry platformServiceEntry);

    Optional<PlatformServiceEntry> findById(String serviceEntryId);

    Collection<PlatformServiceEntry> find(IRegion region, ITenant tenant, String serviceName, String owner);

    Optional<PlatformServiceEntry> findById(IRegion region, ITenant tenant, String serviceEntryId);

    void delete(PlatformServiceEntry serviceEntry);

    boolean isServiceActivationExist(String serviceName);

    void updateServiceEntryState(PlatformServiceEntry entry, PlatformServiceEntryState newState);

    String addTaskInProgress(PlatformServiceEntry entry, PlatformServiceTask task);

    void removeTask(PlatformServiceEntry entry);

    Optional<PlatformServiceEntry> findByAgentServiceEntryId(String agentServiceEntryId);
}
