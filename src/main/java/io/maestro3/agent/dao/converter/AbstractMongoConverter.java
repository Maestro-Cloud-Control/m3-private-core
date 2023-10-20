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


package io.maestro3.agent.dao.converter;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

public abstract class AbstractMongoConverter<X, Y> implements MongoConverter<X, Y> {
    @Autowired
    @Qualifier("baseConverter")
    private MappingMongoConverter mappingMongoConverter;

    protected Document toDocument(X object) {
        Document bson = new Document();
        mappingMongoConverter.write(object, bson);
        return bson;
    }

    protected Y fromDocument(Document document, Class<Y> type) {
        return mappingMongoConverter.read(type, document);
    }
}
