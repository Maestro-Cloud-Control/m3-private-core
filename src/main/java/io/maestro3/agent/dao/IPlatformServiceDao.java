package io.maestro3.agent.dao;

import io.maestro3.agent.platform.model.PlatformService;

import java.util.Optional;

public interface IPlatformServiceDao {

    Optional<PlatformService> findByName(String name);

    void save(PlatformService service);

    void delete(String name);
}
