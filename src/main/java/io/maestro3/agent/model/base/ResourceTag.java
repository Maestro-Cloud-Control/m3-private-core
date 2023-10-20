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

package io.maestro3.agent.model.base;

import io.maestro3.sdk.internal.util.Assert;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceTag implements Comparable<ResourceTag> {

    public static final String TAG_SPLITTER = "=";
    private static final Comparator<ResourceTag> TAG_COMPARATOR =
        Comparator.nullsLast((o1, o2) -> o1.key.compareToIgnoreCase(o2.key));

    private String key;
    private String value;

    public ResourceTag() {
    }

    public ResourceTag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public ResourceTag setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ResourceTag setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        // no checks for 'value' fields because only 'key' determines the uniqueness of the object
        if (this == o) return true;
        if (!(o instanceof ResourceTag)) return false;

        ResourceTag that = (ResourceTag) o;

        return key.equals(that.key);

    }

    @Override
    public int compareTo(ResourceTag that) {
        return TAG_COMPARATOR.compare(this, that);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "Tag{" +
            "key='" + key + '\'' +
            ", value='" + value + '\'' +
            '}';
    }

    public static List<ResourceTag> fromMap(Map<String, String> tags) {
        Assert.notNull(tags, "tags");
        return tags.entrySet().stream()
            .map(entry -> new ResourceTag(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    public static Map<String, String> toMap(Collection<ResourceTag> tags) {
        return tags.stream().collect(Collectors.toMap(ResourceTag::getKey, ResourceTag::getValue));
    }

    public static ResourceTag convertStringToResourceTag(String tag) {
        String[] parsedTag = tag.split(TAG_SPLITTER);
        if (parsedTag.length > 1) {
            return new ResourceTag(parsedTag[0], parsedTag[1]);
        } else {
            return new ResourceTag(parsedTag[0], "");
        }
    }

    public static String convertResourceTagToString(ResourceTag tag) {
        return tag.getKey() + TAG_SPLITTER + tag.getValue();
    }
}
