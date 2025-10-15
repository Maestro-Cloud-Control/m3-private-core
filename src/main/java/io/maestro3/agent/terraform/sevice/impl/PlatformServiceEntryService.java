package io.maestro3.agent.terraform.sevice.impl;

import io.maestro3.agent.dao.IPlatformServiceEntryDao;
import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentInfo;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import io.maestro3.agent.platform.model.PlatformServiceEntryState;
import io.maestro3.agent.terraform.model.PlatformServiceTask;
import io.maestro3.agent.terraform.sevice.IPlatformServiceEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.syndicate.terraform.engine.utils.Asserts;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PlatformServiceEntryService implements IPlatformServiceEntryService {

    private final IPlatformServiceEntryDao serviceEntryDao;

    @Autowired
    public PlatformServiceEntryService(IPlatformServiceEntryDao serviceEntryDao) {
        this.serviceEntryDao = serviceEntryDao;
    }

    @Override
    public void save(final PlatformServiceEntry platformServiceEntry) {
        Asserts.notNull(platformServiceEntry, "platformServiceEntry");
        serviceEntryDao.save(platformServiceEntry);
    }

    @Override
    public Optional<PlatformServiceEntry> findById(final String serviceEntryId) {
        Asserts.notBlank(serviceEntryId, "serviceEntryId");
        return serviceEntryDao.findById(serviceEntryId);
    }

    @Override
    public Collection<PlatformServiceEntry> find(final IRegion region, final ITenant tenant, final String serviceName, final String owner) {
        Asserts.notNull(region, "region");
        Asserts.notNull(tenant, "tenant");
        return serviceEntryDao.find(region.getRegionAlias(), tenant.getTenantAlias(), serviceName, owner);
    }

    @Override
    public Optional<PlatformServiceEntry> findById(final IRegion region, final ITenant tenant, final String serviceEntryId) {
        Asserts.notNull(region, "region");
        Asserts.notNull(tenant, "tenant");
        Asserts.notBlank(serviceEntryId, "serviceEntryId");
        return serviceEntryDao.findById(region.getRegionAlias(), tenant.getTenantAlias(), tenant.getCloud().name(), serviceEntryId);
    }

    @Override
    public void delete(final PlatformServiceEntry serviceEntry) {
        Asserts.notNull(serviceEntry, "serviceEntry");
        serviceEntryDao.delete(serviceEntry);
    }

    @Override
    public boolean isServiceActivationExist(final String serviceName) {
        Asserts.notBlank(serviceName, "serviceName");
        final long activationsCount = serviceEntryDao.count(serviceName);
        return activationsCount > 0;
    }

    @Override
    public void updateServiceEntryState(final PlatformServiceEntry entry, final PlatformServiceEntryState newState) {
        Asserts.notNull(entry, "entry");
        Asserts.notNull(newState, "newState");

        if (entry.getState() == newState) {
            return;
        }

        serviceEntryDao.updateServiceEntrySet(entry.getServiceEntryId(), Collections.singletonMap(PlatformServiceEntry.Fields.STATE, newState));
        entry.withState(newState);
    }

    @Override
    public String addTaskInProgress(final PlatformServiceEntry entry, final PlatformServiceTask task) {
        Asserts.notNull(entry, "entry");
        Asserts.notNull(task, "task");

        final Map<String, String> providerParams = entry.getDeploymentInfo().getProviderParams();
        final String taskId = UUID.randomUUID().toString();
        providerParams.put(PlatformServiceDeploymentInfo.Fields.TASK_ID, taskId);
        providerParams.put(PlatformServiceDeploymentInfo.Fields.TASK_NAME, task.name());

        serviceEntryDao.updateServiceEntrySet(entry.getServiceEntryId(), Map.of(
                getTaskIdField(), taskId,
                getTaskNameField(), task.name()
        ));

        return taskId;
    }

    @Override
    public void removeTask(final PlatformServiceEntry entry) {
        Asserts.notNull(entry, "entry");

        final Map<String, String> providerParams = entry.getDeploymentInfo().getProviderParams();
        providerParams.remove(PlatformServiceDeploymentInfo.Fields.TASK_ID);
        providerParams.remove(PlatformServiceDeploymentInfo.Fields.TASK_NAME);

        serviceEntryDao.updateServiceEntryUnset(entry.getServiceEntryId(), Set.of(getTaskIdField(), getTaskNameField()));
    }

    @Override
    public Optional<PlatformServiceEntry> findByAgentServiceEntryId(final String agentServiceEntryId) {
        Asserts.notBlank(agentServiceEntryId, "agentServiceEntryId");
        return serviceEntryDao.findByAgentServiceEntryId(agentServiceEntryId);
    }

    private String getTaskIdField() {
        return serviceEntryDao.concatenateNestedField(PlatformServiceEntry.Fields.DEPLOYMENT_INFO,
                PlatformServiceDeploymentInfo.Fields.PROVIDER_PARAMS, PlatformServiceDeploymentInfo.Fields.TASK_ID);
    }

    private String getTaskNameField() {
        return serviceEntryDao.concatenateNestedField(PlatformServiceEntry.Fields.DEPLOYMENT_INFO,
                PlatformServiceDeploymentInfo.Fields.PROVIDER_PARAMS, PlatformServiceDeploymentInfo.Fields.TASK_NAME);
    }
}
