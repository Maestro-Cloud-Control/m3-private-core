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

package io.maestro3.agent.api.handler;

import io.maestro3.agent.dao.IRegionRepository;
import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.util.IImageResolver;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.image.SdkImage;
import io.maestro3.sdk.v3.request.agent.ListRegionImagesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ListPrivateRegionImagesHandler extends AbstractM3ApiHandler<ListRegionImagesRequest, List<SdkImage>> {

    private String privateAgentName;
    private Map<PrivateCloudType, IRegionRepository<? extends IRegion>> regionRepositoryMap;
    private Map<PrivateCloudType, IImageResolver> resolverMap = new HashMap<>();

    @Autowired
    public ListPrivateRegionImagesHandler(
        @Value("${private.agent.name}") String privateAgentName,
        Set<IImageResolver> resolvers,
        Set<IRegionRepository<? extends IRegion>> regionRepos) {
        super(ListRegionImagesRequest.class, ActionType.LIST_PRIVATE_REGION_IMAGES);
        this.privateAgentName = privateAgentName;
        this.resolverMap = resolvers.stream()
            .collect(Collectors.toMap(IImageResolver::getCloud, Function.identity()));
        this.regionRepositoryMap = regionRepos.stream()
            .collect(Collectors.toMap(IRegionRepository::getCloud, Function.identity()));
    }

    @Override
    protected List<SdkImage> handlePayload(ActionType action, ListRegionImagesRequest request) {
        if (!request.getPrivateAgentName().equals(privateAgentName)) {
            throw new IllegalArgumentException("Private agent name mismatch. Private agent name is " + privateAgentName +
                ", but name from request is " + request.getPrivateAgentName());
        }
        PrivateCloudType cloudType = PrivateCloudType.fromSdkCloud(request.getCloud());
        IRegionRepository<? extends IRegion> regionRepository = regionRepositoryMap.get(cloudType);
        if (regionRepository == null) {
            throw new IllegalArgumentException("Cloud " + request.getCloud() + " is not supported");
        }
        IRegion region = regionRepository.findByAliasInCloud(request.getNativeRegion());
        if (region == null) {
            throw new IllegalArgumentException("Region " + request.getNativeRegion() + " is not activated in cloud " + cloudType);
        }
        IImageResolver imageResolver = resolverMap.get(cloudType);
        return imageResolver.toSdkImages(region);
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return null;
    }
}
