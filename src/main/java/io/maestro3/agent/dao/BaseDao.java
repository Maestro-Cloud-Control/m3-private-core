package io.maestro3.agent.dao;

import com.mongodb.client.result.UpdateResult;
import io.maestro3.sdk.internal.util.Assert;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class BaseDao<T> {

    private final Class<T> entityClass;
    private final String collectionName;

    @Autowired
    protected MongoTemplate mongoTemplate;

    public BaseDao(String collectionName, Class<T> entityClass) {
        Assert.hasText(collectionName, "collectionName can't be null or empty.");
        Assert.notNull(entityClass, "clazz can't be null.");
        this.collectionName = collectionName;
        this.entityClass = entityClass;
    }

    public T save(T entity) {
        return mongoTemplate.save(entity);
    }

    public List<T> findAll() {
        return mongoTemplate.findAll(entityClass);
    }

    public Optional<T> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, entityClass));
    }

    public void delete(T entity) {
        mongoTemplate.remove(entity);
    }

    public void delete(String id) {
        Assert.hasText(id, "id can't be null or empty.");
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(new ObjectId(id))), collectionName);
    }

    public void update(T object) {
        Assert.notNull(object, "object can't be null.");
        mongoTemplate.save(object, collectionName);
    }

    protected Optional<T> findOne(Query query) {
        return Optional.ofNullable(mongoTemplate.findOne(query, entityClass));
    }

    protected List<T> find(Query query) {
        return mongoTemplate.find(query, entityClass);
    }

    protected long count(Query query) {
        return mongoTemplate.count(query, entityClass);
    }

    protected UpdateResult updateFirst(Query query, Update update) {
        return mongoTemplate.updateFirst(query, update, entityClass);
    }

    public void modifyAll(Query query, Update update) {
        Assert.notNull(update, "update can't be null.");
        Assert.isTrue(!update.getUpdateObject().keySet().isEmpty(), "update must not be empty (it will delete the document!)");
        mongoTemplate.updateMulti(query, update, this.collectionName);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> distinct(Query query, String key) {
        return (List<T>) mongoTemplate.findDistinct(query, key, this.collectionName, entityClass);
    }

    protected void remove(Query query) {
        mongoTemplate.remove(query, entityClass);
    }

    protected boolean exists(Query query) {
        return mongoTemplate.exists(query, entityClass);
    }

    protected Collection<T> aggregate(final Aggregation aggregation) {
        final AggregationResults<T> aggregationResults = mongoTemplate.aggregate(aggregation, entityClass, entityClass);
        return aggregationResults.getMappedResults();
    }

    public String concatenateNestedField(String... fields) {
        return String.join(".", fields);
    }
}
