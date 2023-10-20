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

package io.maestro3.agent.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class ConversionUtils {

    public static <T, R extends T> List<R> convertToSubclass(Collection<T> objects, Class<R> resultType) {
        return Optional.ofNullable(objects).orElse(Collections.emptyList())
            .stream()
            .map(resultType::cast)
            .collect(Collectors.toList());
    }

    public static <F, T> List<T> castCollection(Collection<? extends F> collection, final Class<T> type) {
        return convertCollection(collection, (IConversionFunction<F, T>) type::cast);
    }

    public static <T> List generify(Collection collection, final Class<T> type) {
        return convertCollection(collection, type::cast);
    }

    public static <T> List<T> notNull(List<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public static <F, T> List<T> convertCollection(Collection<? extends F> fromCollection, IConversionFunction<F, T> conversionFunction) {
        if (CollectionUtils.isEmpty(fromCollection)) {
            return Collections.emptyList();
        }
        List<T> resultCollection = new ArrayList<>(fromCollection.size());
        for (F item : fromCollection) {
            T res = convert(item, conversionFunction);
            if (res != null) {
                resultCollection.add(res);
            }
        }
        return resultCollection;
    }

    public static <F, T> Set<T> convertSet(Collection<? extends F> fromSet, IConversionFunction<F, T> conversionFunction) {
        if (CollectionUtils.isEmpty(fromSet)) {
            return Collections.emptySet();
        }
        Set<T> resultSet = new LinkedHashSet<>();
        for (F item : fromSet) {
            T res = convert(item, conversionFunction);
            if (res != null) {
                resultSet.add(res);
            }
        }
        return resultSet;
    }

    public static <F, T> T convert(F from, IConversionFunction<F, T> conversionFunction) {
        if (from == null) {
            return null;
        }
        return conversionFunction.convert(from);
    }

    public static <F, T> Map<T, F> convertToMap(Collection<F> from, IConversionFunction<F, T> conversionFunction) {
        if (CollectionUtils.isEmpty(from)) {
            return Collections.emptyMap();
        }
        Map<T, F> result = new HashMap<>();
        for (F element : from) {
            result.put(conversionFunction.convert(element), element);
        }
        return result;
    }

    public static <F, T> Multimap<T, F> convertToMultimap(Collection<F> from, IConversionFunction<F, T> conversionFunction) {
        ArrayListMultimap<T, F> result = ArrayListMultimap.create();
        if (CollectionUtils.isEmpty(from)) {
            return result;
        }
        for (F element : from) {
            result.put(conversionFunction.convert(element), element);
        }
        return result;
    }

    public static <E, K, V> List<E> mapToList(Map<K, V> map, IConversionFunction<Map.Entry<K, V>, E> conversionFunction) {
        if (MapUtils.isEmpty(map)) {
            return Collections.emptyList();
        }

        List<E> result = new ArrayList<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            result.add(conversionFunction.convert(entry));
        }
        return result;
    }
}
