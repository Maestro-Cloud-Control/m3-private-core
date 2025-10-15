package io.maestro3.agent.dao.impl;

import io.maestro3.agent.dao.BaseDao;
import io.maestro3.agent.dao.IPlatformServiceEntryDao;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentInfo;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import team.syndicate.terraform.engine.utils.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Component
public class PlatformServiceEntryDao extends BaseDao<PlatformServiceEntry> implements IPlatformServiceEntryDao {

    @Autowired
    public PlatformServiceEntryDao() {
        super("PlatformServiceEntries", PlatformServiceEntry.class);
    }

    @Override
    public Collection<PlatformServiceEntry> find(final String regionName, final String tenantName, final String serviceName, final String owner) {
        Criteria searchCriteria = Criteria.where(PlatformServiceEntry.Fields.TENANT_NAME).is(tenantName.toUpperCase())
                .and(PlatformServiceEntry.Fields.REGION_NAME).is(regionName);
        if (StringUtils.isNotBlank(serviceName)) {
            searchCriteria.and(PlatformServiceEntry.Fields.SERVICE_NAME).is(serviceName);
        }
        if (StringUtils.isNotBlank(owner)) {
            searchCriteria.and(PlatformServiceEntry.Fields.OWNER).is(owner);
        }
        return super.find(Query.query(searchCriteria));
    }

    @Override
    public Optional<PlatformServiceEntry> findById(final String regionName, final String tenantName, final String cloud,
                                                   final String serviceEntryId) {
        CriteriaDefinition searchCriteria = Criteria.where(PlatformServiceEntry.Fields.REGION_NAME).is(regionName)
                .and(PlatformServiceEntry.Fields.TENANT_NAME).is(tenantName.toUpperCase())
                .and(PlatformServiceEntry.Fields.CLOUD).is(cloud.toUpperCase())
                .and(PlatformServiceEntry.Fields.SERVICE_ENTRY_ID).is(serviceEntryId);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public long count(final String serviceName) {
        CriteriaDefinition searchCriteria = Criteria.where(PlatformServiceEntry.Fields.SERVICE_NAME).is(serviceName);
        return super.count(Query.query(searchCriteria));
    }

    @Override
    public void updateServiceEntrySet(final String serviceEntryId, final Map<String, Object> fieldsToUpdate) {
        Query query = Query.query(Criteria.where(PlatformServiceEntry.Fields.SERVICE_ENTRY_ID).is(serviceEntryId));
        Update update = new Update();
        fieldsToUpdate.forEach(update::set);
        super.updateFirst(query, update);
    }

    @Override
    public void updateServiceEntryUnset(final String serviceEntryId, final Collection<String> fieldsToUnset) {
        Query query = Query.query(Criteria.where(PlatformServiceEntry.Fields.SERVICE_ENTRY_ID).is(serviceEntryId));
        Update update = new Update();
        fieldsToUnset.forEach(update::unset);
        super.updateFirst(query, update);
    }

    @Override
    public Optional<PlatformServiceEntry> findByAgentServiceEntryId(final String agentServiceEntryId) {
        final String agentServiceEntryIdField = concatenateNestedField(PlatformServiceEntry.Fields.DEPLOYMENT_INFO,
                PlatformServiceDeploymentInfo.Fields.PROVIDER_PARAMS, PlatformServiceDeploymentInfo.Fields.PRIVATE_AGENT_SERVICE_ENTRY_ID);
        final CriteriaDefinition searchCriteria = Criteria.where(agentServiceEntryIdField).is(agentServiceEntryId);
        return super.findOne(Query.query(searchCriteria));
    }
}
