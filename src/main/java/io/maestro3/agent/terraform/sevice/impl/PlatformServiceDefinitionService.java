package io.maestro3.agent.terraform.sevice.impl;

import io.maestro3.agent.dao.IPlatformServiceDefinitionDao;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.platform.model.PlatformServiceDefinition;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;
import io.maestro3.agent.terraform.sevice.IPlatformServiceDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.utils.Asserts;

import java.util.Collection;
import java.util.Optional;

@Service
public class PlatformServiceDefinitionService implements IPlatformServiceDefinitionService {

    private final IPlatformServiceDefinitionDao serviceDefinitionDao;

    @Autowired
    public PlatformServiceDefinitionService(IPlatformServiceDefinitionDao serviceDefinitionDao) {
        this.serviceDefinitionDao = serviceDefinitionDao;
    }

    @Override
    public boolean isServiceExist(final String serviceName) {
        Asserts.notBlank(serviceName, "serviceName");
        return serviceDefinitionDao.isServiceDefinitionExist(serviceName);
    }

    @Override
    public void save(final PlatformServiceDefinition serviceDefinition) {
        Asserts.notNull(serviceDefinition, "serviceDefinition");
        serviceDefinitionDao.save(serviceDefinition);
    }

    @Override
    public Optional<PlatformServiceDefinition> findByName(final String serviceName) {
        Asserts.notBlank(serviceName, "serviceName");
        return serviceDefinitionDao.findByName(serviceName);
    }

    @Override
    public Collection<PlatformServiceDefinition> findByTenant(final ITenant tenant,
                                                              final PlatformServiceDeploymentType deploymentType) {
        Asserts.notNull(tenant, "tenant");
        return serviceDefinitionDao.findByTenant(tenant.getTenantAlias(), tenant.getCloud().name(), deploymentType);
    }

    @Override
    public Optional<PlatformServiceDefinition> findByNameAndTenant(final String serviceName, final ITenant tenant) {
        Asserts.notBlank(serviceName, "serviceName");
        Asserts.notNull(tenant, "tenant");
        return serviceDefinitionDao.findByNameAndTenant(serviceName, tenant.getTenantAlias(), tenant.getCloud().name());
    }

    @Override
    public Optional<PlatformServiceDefinition> findByServiceName(final String serviceName,
                                                                 final PlatformServiceDeploymentType deploymentType) {
        Asserts.notBlank(serviceName, "serviceName");
        return serviceDefinitionDao.find(serviceName, deploymentType);
    }

    @Override
    public Optional<PlatformServiceDefinition> findByNameAndPrivateAgentId(final String serviceName, final String privateAgentId) {
        Asserts.notBlank(serviceName, "serviceName");
        Asserts.notBlank(privateAgentId, "privateAgentId");
        return serviceDefinitionDao.findByNameAndPrivateAgentId(serviceName, privateAgentId);
    }

    @Override
    public void delete(final PlatformServiceDefinition serviceDefinition) {
        Asserts.notNull(serviceDefinition, "serviceDefinition");
        serviceDefinitionDao.delete(serviceDefinition);
    }
}
