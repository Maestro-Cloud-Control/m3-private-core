package io.maestro3.agent.dao;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.Optional;

public abstract class BaseTemplatesDao<T> {

    private final MongoTemplate mongoTemplate;
    private final Class<T> entityClass;

    protected BaseTemplatesDao(MongoTemplate mongoTemplate,
                               Class<T> entityClass) {
        this.mongoTemplate = mongoTemplate;
        this.entityClass = entityClass;
    }

    public void save(T entity) {
        mongoTemplate.save(entity);
    }

    protected Optional<T> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, entityClass));
    }

    protected Optional<T> findOne(Query query) {
        return Optional.ofNullable(mongoTemplate.findOne(query, entityClass));
    }

    protected Collection<T> find(Query query) {
        return mongoTemplate.find(query, entityClass);
    }

    protected long count(Query query) {
        return mongoTemplate.count(query, entityClass);
    }

    protected UpdateResult updateFirst(Query query, Update update) {
        return mongoTemplate.updateFirst(query, update, entityClass);
    }

    protected void remove(Query query) {
        mongoTemplate.remove(query, entityClass);
    }

    protected String concatenateNestedField(String... fields) {
        return String.join(".", fields);
    }
}
