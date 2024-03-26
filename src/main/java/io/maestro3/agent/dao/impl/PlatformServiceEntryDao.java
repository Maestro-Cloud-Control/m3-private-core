package io.maestro3.agent.dao.impl;

import io.maestro3.agent.dao.BaseTemplatesDao;
import io.maestro3.agent.dao.IPlatformServiceEntryDao;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PlatformServiceEntryDao extends BaseTemplatesDao<PlatformServiceEntry> implements IPlatformServiceEntryDao {

    @Autowired
    public PlatformServiceEntryDao(MongoTemplate mongoTemplate) {
        super(mongoTemplate, PlatformServiceEntry.class);
    }

    @Override
    public Optional<PlatformServiceEntry> findById(String id) {
        CriteriaDefinition searchCriteria = Criteria.where(PlatformServiceEntry.ID_FIELD).is(id);
        return super.findOne(Query.query(searchCriteria));
    }

    @Override
    public void delete(String id) {
        Query query = Query.query(Criteria.where(PlatformServiceEntry.ID_FIELD).is(id));
        super.remove(query);
    }
}
