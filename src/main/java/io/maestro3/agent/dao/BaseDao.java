
/*
 * Copyright 2023 Maestro Cloud Control LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.maestro3.agent.dao;

import com.google.common.util.concurrent.AtomicLongMap;
import io.maestro3.sdk.internal.util.StringUtils;
import com.mongodb.client.result.UpdateResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseDao<T> {

    private static final Logger LOG = LogManager.getLogger(BaseDao.class);

    private static AtomicLongMap<String> writeOperations = AtomicLongMap.create();

    private final String collectionName;
    private final String collectionPreventedUpdates;
    private final Class<T> clazz;

    @Autowired
    protected MongoTemplate template;

    public BaseDao(String collectionName, Class<T> clazz) {
        Assert.hasText(collectionName, "collectionName can't be null or empty.");
        Assert.notNull(clazz, "clazz can't be null.");
        this.collectionName = collectionName;
        this.collectionPreventedUpdates = collectionName + "_prevented_updates_count";
        this.clazz = clazz;
    }

    public static Map<String, Long> getStatistics() {
        return new HashMap<>(writeOperations.asMap());
    }

    public static void clearStatistics() {
        writeOperations.clear();
    }

    public void save(T object) {
        Assert.notNull(object, "object can't be null.");
        mongo().save(object, collectionName);
        writeOperations.incrementAndGet(collectionName);
    }

    public void update(T object) {
        Assert.notNull(object, "object can't be null.");

        if (shouldNotBeUpdated(object)) {
            increasePreventedUpdatesCount();
        }

        mongo().save(object, collectionName);
        writeOperations.incrementAndGet(collectionName);
    }

    protected boolean shouldNotBeUpdated(T object) {
        try {

            Document dbObject = new Document();
            mongo().getConverter().write(object, dbObject);
            String json = dbObject.toJson();
            if (StringUtils.isBlank(json)) {
                return false;
            }
        } catch (Exception ex) {
            LOG.error(String.format("Failed to calculate digest for %s", collectionName), ex);
        }
        return false;
    }

    public T findById(String id) {
        Assert.hasText(id, "entityID can't be null or empty.");
        return mongo().findById(new ObjectId(id), this.clazz, collectionName);
    }

    public List<T> findAll() {
        return mongo().findAll(this.clazz, collectionName);
    }

    public void delete(String id) {
        Assert.hasText(id, "id can't be null or empty.");

        mongo().remove(Query.query(Criteria.where("_id").is(new ObjectId(id))), collectionName);
        writeOperations.incrementAndGet(collectionName);
    }

    public void delete(List<String> ids) {
        Assert.notEmpty(ids, "ids can't be null or empty");

        remove(Query.query(Criteria.where("_id").in(ids)));
    }

    protected void remove(Query query) {
        track(query);
        mongo().remove(query, collectionName);
        writeOperations.incrementAndGet(collectionName);
    }

    public long count() {
        return mongo().count(new Query(), collectionName);
    }

    public void insert(T object) {
        Assert.notNull(object, "object can't be null.");
        mongo().insert(object, this.collectionName);
        writeOperations.incrementAndGet(collectionName);
    }

    public void insertAll(Collection<? extends T> objects) {
        Assert.notEmpty(objects, "collection can't be null or empty.");
        mongo().insert(objects, collectionName);
        writeOperations.incrementAndGet(collectionName);
    }

    protected long count(Query query) {
        track(query);
        return mongo().count(query, collectionName);
    }

    public List<T> findAll(Query query) {
        track(query);
        return mongo().find(query, this.clazz, this.collectionName);
    }

    protected List<String> objectIdToStringCollection(List<ObjectId> dbResult) {
        if (dbResult == null) {
            return null;
        }
        List<String> result = new LinkedList<>();
        for (ObjectId id : dbResult) {
            result.add(id.toString());
        }
        return result;
    }

    protected <Z extends T> List<Z> findAll(Query query, Class<Z> clazz) {
        track(query);
        return mongo().find(query, clazz, this.collectionName);
    }

    protected T findAndModify(Query query, Update update) {
        Assert.notNull(update, "update can't be null.");
        Assert.isTrue(!update.getUpdateObject().keySet().isEmpty(), "update must not be empty (it will delete the document!)");
        track(query);
        writeOperations.incrementAndGet(collectionName);
        return mongo().findAndModify(query, update, this.clazz, this.collectionName);
    }

    protected T findOne(Query query) {
        track(query);
        return mongo().findOne(query, this.clazz, this.collectionName);
    }

    protected <Z extends T> Z findOne(Query query, Class<Z> clazz) {
        track(query);
        return mongo().findOne(query, clazz, this.collectionName);
    }

    public void modifyOne(Query query, Update update) {
        Assert.notNull(update, "update can't be null.");
        Assert.isTrue(!update.getUpdateObject().keySet().isEmpty(), "update must not be empty (it will delete the document!)");
        track(query);
        writeOperations.incrementAndGet(collectionName);
        mongo().updateFirst(query, update, this.collectionName);
    }

    protected UpdateResult upsert(Query query, Update update) {
        Assert.notNull(update, "update can't be null.");
        track(query);
        writeOperations.incrementAndGet(collectionName);
        return mongo().upsert(query, update, this.collectionName);
    }

    public void modifyAll(Query query, Update update) {
        Assert.notNull(update, "update can't be null.");
        Assert.isTrue(!update.getUpdateObject().keySet().isEmpty(), "update must not be empty (it will delete the document!)");
        track(query);
        writeOperations.incrementAndGet(collectionName);
        mongo().updateMulti(query, update, this.collectionName);
    }

    protected void ensureIndex(Index idx) {
        Assert.notNull(idx, "idx can't be null.");
        Assert.isTrue(idx.getIndexKeys().keySet().size() > 0, "idx.indexKeys are empty.");

        this.mongo().indexOps(this.collectionName).ensureIndex(idx);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> distinct(Query query, String key) {
        track(query);
        return (List<T>) mongo().findDistinct(query, key, this.collectionName, clazz);
    }

    protected List<String> toLowerCaseList(Collection<String> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return new LinkedList<>();
        }

        List<String> resultList = new LinkedList<>();
        for (String item : collection) {
            if (item != null) {
                resultList.add(item.toLowerCase());
            }
        }

        return resultList;
    }

    protected Set<String> toLowerCaseSet(Collection<String> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return new HashSet<>();
        }

        Set<String> result = new HashSet<>();
        for (String item : collection) {
            if (item != null) {
                result.add(item.toLowerCase());
            }
        }

        return result;
    }


    public List<T> findAll(int skip, int limit) {
        Query query = new Query().skip(skip).limit(limit).with(Sort.by(Sort.Direction.ASC, "_id"));
        track(query);
        return this.findAll(query);
    }

    public String getCollection() {
        return this.collectionName;
    }

    public Class<T> getCollectionClass() {
        return clazz;
    }

    private void track(Query query) {
        //implement query tracking
    }

    protected void increasePreventedUpdatesCount() {
        writeOperations.incrementAndGet(collectionPreventedUpdates);
    }

    protected MongoOperations mongo() {
        return this.template;
    }
}
