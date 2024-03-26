package io.maestro3.agent.dao.impl;

import com.mongodb.client.result.UpdateResult;
import io.maestro3.agent.dao.BaseTemplatesDao;
import io.maestro3.agent.dao.ITerraformTemplateDao;
import io.maestro3.agent.terraform.model.TerraformStack;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
public class TerraformTemplateDao extends BaseTemplatesDao<TerraformTemplate> implements ITerraformTemplateDao {

    @Autowired
    public TerraformTemplateDao(MongoTemplate mongoTemplate) {
        super(mongoTemplate, TerraformTemplate.class);
    }

    private static Query getTemplateIdQuery(String templateId) {
        return Query.query(Criteria.where(TerraformTemplate.Fields.TEMPLATE_ID).is(templateId));
    }

    @Override
    public void delete(String templateId) {
        Query query = getTemplateIdQuery(templateId);
        super.remove(query);
    }

    @Override
    public Optional<TerraformTemplate> findByTemplateId(String templateId) {
        return super.findById(templateId);
    }

    @Override
    public Optional<TerraformTemplate> findByNameAndTenant(String name, String tenantName) {
        CriteriaDefinition searchCriteria = Criteria.where(TerraformTemplate.Fields.NAME).is(name)
                .and(TerraformTemplate.Fields.TENANT_NAME).is(tenantName.toUpperCase());
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public void save(TerraformTemplate template){
        template.withTenantName(template.getTenantName().toUpperCase());
        super.save(template);
    }

    @Override
    public Collection<TerraformTemplate> findByNamesAndTenant(String tenantName, Collection<String> names) {
        Criteria searchCriteria = Criteria.where(TerraformTemplate.Fields.TENANT_NAME).is(tenantName.toUpperCase());
        if (CollectionUtils.isNotEmpty(names)) {
            searchCriteria.and(TerraformTemplate.Fields.NAME).in(names);
        }
        return super.find(Query.query(searchCriteria));
    }

    @Override
    public boolean updateTemplateSet(String templateId, Map<String, Object> fieldsToUpdate) {
        Query query = getTemplateIdQuery(templateId);
        Update update = new Update();
        fieldsToUpdate.forEach(update::set);
        UpdateResult updateResult = super.updateFirst(query, update);
        return updateResult.getModifiedCount() == 1;
    }

    @Override
    public boolean updateTemplateUnset(String templateId, Collection<String> fieldsToUpdate) {
        Query query = getTemplateIdQuery(templateId);
        Update update = new Update();
        fieldsToUpdate.forEach(update::unset);
        UpdateResult updateResult = super.updateFirst(query, update);
        return updateResult.getModifiedCount() == 1;
    }

    @Override
    public boolean updateTemplateAddToSet(String templateId, String field, Object valueToAdd) {
        Query query = getTemplateIdQuery(templateId);
        Update update = new Update().addToSet(field, valueToAdd);
        UpdateResult updateResult = super.updateFirst(query, update);
        return updateResult.getModifiedCount() == 1;
    }

    @Override
    public void pollQueuedTask(String templateId) {
        Query query = getTemplateIdQuery(templateId);
        Update update = new Update().pop(TerraformTemplate.Fields.AUTO_TASK_QUEUE, Update.Position.FIRST);
        super.updateFirst(query, update);
    }

    @Override
    public Collection<TerraformTemplate> findTemplatesWithQueuedTasks() {
        CriteriaDefinition searchCriteria = Criteria.where(TerraformTemplate.Fields.TASKS_IN_PROGRESS).is(Collections.emptyMap())
                .and(TerraformTemplate.Fields.AUTO_TASK_QUEUE).gt(Collections.emptyList());
        return super.find(Query.query(searchCriteria));
    }
}
