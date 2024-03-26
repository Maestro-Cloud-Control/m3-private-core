package io.maestro3.agent.dao.impl;

import io.maestro3.agent.dao.BaseTemplatesDao;
import io.maestro3.agent.dao.ITerraformStackDao;
import io.maestro3.agent.terraform.model.TerraformStack;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Component
public class TerraformStackDao extends BaseTemplatesDao<TerraformStack> implements ITerraformStackDao {

    @Autowired
    public TerraformStackDao(MongoTemplate mongoTemplate) {
        super(mongoTemplate, TerraformStack.class);
    }

    @Override
    public Optional<TerraformStack> findByTemplateId(String templateId) {
        CriteriaDefinition searchCriteria = Criteria.where(TerraformStack.Fields.TEMPLATE_ID).is(templateId);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public void save(TerraformStack stack){
        stack.withTenantName(stack.getTenantName().toUpperCase());
        super.save(stack);
    }

    @Override
    public Optional<TerraformStack> findByStackId(String stackId) {
        CriteriaDefinition searchCriteria = Criteria.where(TerraformStack.Fields.STACK_ID).is(stackId);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public Optional<TerraformStack> findByEntryID(String entryId) {
        CriteriaDefinition searchCriteria = Criteria.where(TerraformStack.Fields.PAAS).is(entryId);
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
