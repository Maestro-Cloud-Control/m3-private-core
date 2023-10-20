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

import io.maestro3.agent.model.base.IWithTags;
import io.maestro3.agent.model.base.ResourceTag;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.model.instance.SdkResourceTag;
import io.maestro3.sdk.v3.model.instance.SdkResourceTagDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public final class TagUtils {

    private TagUtils() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }

    public static <T extends IWithTags> T mergeTags(T resourceWithTags, Map<String, String> tags) {
        List<ResourceTag> tagsToUpdate = ResourceTag.fromMap(tags);
        return mergeTags(resourceWithTags, tagsToUpdate);
    }

    public static <T extends IWithTags> T mergeTags(T resourceWithTags, Set<String> tags) {
        List<ResourceTag> tagsToUpdate = CollectionUtils.map(tags, ResourceTag::convertStringToResourceTag);
        return mergeTags(resourceWithTags, tagsToUpdate);
    }

    private static <T extends IWithTags> T mergeTags(T resourceWithTags, List<ResourceTag> tagsToUpdate) {
        List<ResourceTag> resourceTags = resourceWithTags.getTags();
        resourceTags.removeAll(tagsToUpdate);
        resourceTags.addAll(tagsToUpdate);
        Collections.sort(resourceTags);
        resourceWithTags.setTags(resourceTags);
        return resourceWithTags;
    }

    public static <T extends IWithTags> T removeTags(T instance, Map<String, String> tags) {
        List<ResourceTag> tagsToRemove = ResourceTag.fromMap(tags);
        return removeTags(instance, tagsToRemove);
    }

    public static <T extends IWithTags> T removeTags(T instance, Set<String> tags) {
        List<ResourceTag> tagsToRemove = tags.stream()
            .map(ResourceTag::convertStringToResourceTag)
            .collect(Collectors.toList());
        return removeTags(instance, tagsToRemove);
    }

    private static <T extends IWithTags> T removeTags(T instance, List<ResourceTag> tagsToRemove) {
        List<ResourceTag> resourceTags = instance.getTags();
        resourceTags.removeAll(tagsToRemove);
        instance.setTags(resourceTags);
        return instance;
    }

    public static <T extends IWithTags> T removeTagsByKeys(T instance, Set<String> tagKeys) {
        List<ResourceTag> resourceTags = instance.getTags();
        resourceTags.removeIf(resourceTag -> tagKeys.contains(resourceTag.getKey()));
        instance.setTags(resourceTags);
        return instance;
    }

    public static Set<String> extractTagKeys(Set<String> tags) {
        return tags.stream()
            .map(ResourceTag::convertStringToResourceTag)
            .map(ResourceTag::getKey)
            .collect(Collectors.toSet());
    }

    public static List<SdkResourceTag> convertTagsToSdkTags(List<ResourceTag> tags) {
        if (tags == null) {
            return Collections.emptyList();
        }
        List<SdkResourceTag> sdkTags = new ArrayList<>(tags.size());
        for (ResourceTag tag : tags) {
            sdkTags.add(new SdkResourceTag(tag.getKey(), tag.getValue()));
        }
        return sdkTags;
    }

    public static List<SdkResourceTagDto> convertResourceTagToSdkResourceTagDto(List<ResourceTag> tags) {
        return convertTagsToSdkTags(tags).stream()
            .map(SdkResourceTagDto::new)
            .collect(Collectors.toList());
    }

    public static boolean isTagsChanged(IWithTags vmFromProvider, IWithTags vmFromRepository) {
        if (vmFromProvider.getTags().size() != vmFromRepository.getTags().size()) {
            return true;
        }
        Map<String, String> tagsFromProvider = vmFromProvider.getTags()
            .stream()
            .collect(Collectors.toMap(ResourceTag::getKey, ResourceTag::getValue, (t1, t2) -> t1));
        Map<String, String> tagsFromRepo = vmFromRepository.getTags()
            .stream()
            .collect(Collectors.toMap(ResourceTag::getKey, ResourceTag::getValue, (t1, t2) -> t1));
        for (Map.Entry<String, String> entry : tagsFromProvider.entrySet()) {
            String tagValueFromRepo = tagsFromRepo.remove(entry.getKey());
            if (StringUtils.isBlank(tagValueFromRepo) || !tagValueFromRepo.equals(entry.getValue())) {
                return true;
            }
        }
        return tagsFromRepo.size() != 0;
    }
}
