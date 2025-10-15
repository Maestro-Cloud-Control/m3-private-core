package io.maestro3.agent.dao.impl;

import io.maestro3.agent.dao.BaseDao;
import io.maestro3.agent.dao.IPlatformServiceDefinitionDao;
import io.maestro3.agent.platform.model.PlatformServiceDefinition;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentInfo;
import io.maestro3.agent.platform.model.PlatformServiceDeploymentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class PlatformServiceDefinitionDao extends BaseDao<PlatformServiceDefinition> implements IPlatformServiceDefinitionDao {

    @Autowired
    public PlatformServiceDefinitionDao() {
        super("PlatformServiceDefinitions", PlatformServiceDefinition.class);
    }

    @Override
    public boolean isServiceDefinitionExist(final String serviceName) {
        CriteriaDefinition searchCriteria = Criteria.where(PlatformServiceDefinition.Fields.NAME).is(serviceName);
        return super.exists(Query.query(searchCriteria));
    }

    @Override
    public Optional<PlatformServiceDefinition> findByName(final String serviceName) {
        CriteriaDefinition searchCriteria = Criteria.where(PlatformServiceDefinition.Fields.NAME).is(serviceName);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public Optional<PlatformServiceDefinition> findByNameAndTenant(final String serviceName,
                                                                   final String tenantDisplayName,
                                                                   final String cloud) {
        final Criteria searchCriteria = Criteria.where(PlatformServiceDefinition.Fields.NAME).is(serviceName);
        addTenantCriteria(searchCriteria, tenantDisplayName, cloud);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public Collection<PlatformServiceDefinition> findByTenant(final String tenantDisplayName, final String cloud,
                                                              final PlatformServiceDeploymentType deploymentType) {
        final Criteria searchCriteria = new Criteria();
        addTenantCriteria(searchCriteria, tenantDisplayName, cloud);
        addOptionalDeploymentType(searchCriteria, deploymentType);
        return super.find(Query.query(searchCriteria));
    }

    @Override
    public Optional<PlatformServiceDefinition> find(final String serviceName,
                                                    final PlatformServiceDeploymentType deploymentType) {
        Criteria searchCriteria = Criteria.where(PlatformServiceDefinition.Fields.NAME).is(serviceName);
        addOptionalDeploymentType(searchCriteria, deploymentType);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public void deleteByName(String name) {
        Criteria searchCriteria = Criteria.where(PlatformServiceDefinition.Fields.NAME).is(name);
        super.remove(Query.query(searchCriteria));
    }

    @Override
    public Optional<PlatformServiceDefinition> findByNameAndPrivateAgentId(final String serviceName, final String privateAgentId) {
        final String privateAgentIdField = concatenateNestedField(PlatformServiceDefinition.Fields.SERVICE_DEPLOYMENT_INFO,
                PlatformServiceDeploymentInfo.Fields.PROVIDER_PARAMS, PlatformServiceDeploymentInfo.Fields.PRIVATE_AGENT_ID);
        CriteriaDefinition searchCriteria = Criteria.where(PlatformServiceDefinition.Fields.NAME).is(serviceName)
                .and(privateAgentIdField).is(privateAgentId);
        return super.findOne(Query.query(searchCriteria));
    }

    private void addTenantCriteria(final Criteria searchCriteria, final String tenantDisplayName, final String cloud) {
        searchCriteria.and(PlatformServiceDefinition.Fields.SUPPORTED_CLOUDS).is(cloud)
                .orOperator(
                        Criteria.where(PlatformServiceDefinition.Fields.TENANT_DISPLAY_NAME).is(tenantDisplayName),
                        Criteria.where(PlatformServiceDefinition.Fields.TENANT_DISPLAY_NAME).exists(false)
                );
    }

    private void addOptionalDeploymentType(final Criteria searchCriteria, final PlatformServiceDeploymentType deploymentType) {
        if (deploymentType != null) {
            final String providerTypeField = concatenateNestedField(
                    PlatformServiceDefinition.Fields.SERVICE_DEPLOYMENT_INFO, PlatformServiceDeploymentInfo.Fields.PROVIDER_TYPE);
            searchCriteria.and(providerTypeField).is(deploymentType);
        }
    }
}
