package io.maestro3.agent.dao.impl;

import io.maestro3.agent.dao.BaseDao;
import io.maestro3.agent.dao.ITerraformStackDao;
import io.maestro3.agent.terraform.model.TerraformStack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import team.syndicate.terraform.engine.utils.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Component
public class TerraformStackDao extends BaseDao<TerraformStack> implements ITerraformStackDao {

    @Autowired
    public TerraformStackDao() {
        super("TerraformStacks", TerraformStack.class);
    }

    @Override
    public Optional<TerraformStack> findByTemplateId(String templateId) {
        CriteriaDefinition searchCriteria = Criteria.where(TerraformStack.Fields.TEMPLATE_ID).is(templateId);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public Optional<TerraformStack> findByStackId(String stackId) {
        CriteriaDefinition searchCriteria = Criteria.where(TerraformStack.Fields.STACK_ID).is(stackId);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public Collection<TerraformStack> findByTemplateName(String tenantName, String templateName) {
        CriteriaDefinition searchCriteria = Criteria.where(TerraformStack.Fields.TENANT_NAME).is(tenantName.toUpperCase())
                .and(TerraformStack.Fields.TEMPLATE_NAME).is(templateName);
        return super.find(Query.query(searchCriteria));
    }

    @Override
    public Optional<TerraformStack> findByTemplateName(String tenantName, String templateName, String stackId) {
        CriteriaDefinition searchCriteria = Criteria.where(TerraformStack.Fields.TENANT_NAME).is(tenantName.toUpperCase())
                .and(TerraformStack.Fields.TEMPLATE_NAME).is(templateName).and(TerraformStack.Fields.STACK_ID).is(stackId);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public Optional<TerraformStack> findByServiceEntryId(final String serviceEntryId) {
        CriteriaDefinition searchCriteria = Criteria.where(TerraformStack.Fields.SERVICE_ENTRY_ID).is(serviceEntryId);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public void updateStack(String stackId, Map<String, Object> fieldsToUpdate) {
        Query query = Query.query(Criteria.where(TerraformStack.Fields.STACK_ID).is(stackId));
        Update update = new Update();
        fieldsToUpdate.forEach(update::set);
        super.updateFirst(query, update);
    }

    @Override
    public Collection<TerraformStack> findByTenant(String tenantName, Collection<String> stackIds) {
        Criteria searchCriteria = Criteria.where(TerraformStack.Fields.TENANT_NAME).is(tenantName.toUpperCase());
        if (CollectionUtils.isNotEmpty(stackIds)) {
            searchCriteria.and(TerraformStack.Fields.STACK_ID).in(stackIds);
        }
        return super.find(Query.query(searchCriteria));
    }

    @Override
    public void delete(String stackId) {
        Query query = Query.query(Criteria.where(TerraformStack.Fields.STACK_ID).is(stackId));
        super.remove(query);
    }
}
