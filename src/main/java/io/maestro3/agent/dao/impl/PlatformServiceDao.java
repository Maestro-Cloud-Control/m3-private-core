package io.maestro3.agent.dao.impl;

import io.maestro3.agent.dao.BaseTemplatesDao;
import io.maestro3.agent.dao.IPlatformServiceDao;
import io.maestro3.agent.platform.model.PlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PlatformServiceDao extends BaseTemplatesDao<PlatformService> implements IPlatformServiceDao {

    @Autowired
    public PlatformServiceDao(MongoTemplate mongoTemplate) {
        super(mongoTemplate, PlatformService.class);
    }

    @Override
    public Optional<PlatformService> findByName(String name) {
        CriteriaDefinition searchCriteria = Criteria.where(PlatformService.NAME_FIELD).is(name);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public void delete(String name) {
        Query query = Query.query(Criteria.where(PlatformService.NAME_FIELD).is(name));
        super.remove(query);
    }
}
