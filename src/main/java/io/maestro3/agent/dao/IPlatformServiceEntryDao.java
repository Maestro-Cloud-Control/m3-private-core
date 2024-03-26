package io.maestro3.agent.dao;

import io.maestro3.agent.platform.model.PlatformServiceEntry;

import java.util.Optional;

public interface IPlatformServiceEntryDao {

    Optional<PlatformServiceEntry> findById(String id);

    void save(PlatformServiceEntry service);

    void delete(String name);
}
