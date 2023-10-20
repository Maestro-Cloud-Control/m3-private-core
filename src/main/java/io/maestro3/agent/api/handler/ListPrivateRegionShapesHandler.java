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
import io.maestro3.agent.model.base.ShapeConfig;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.SdkPrivateAgentShapeInfo;
import io.maestro3.sdk.v3.request.agent.ListNativeShapesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ListPrivateRegionShapesHandler extends AbstractM3ApiHandler<ListNativeShapesRequest, List<SdkPrivateAgentShapeInfo>> {

    private String privateAgentName;
    private Map<PrivateCloudType, IRegionRepository<? extends IRegion<? extends ShapeConfig>>> regionRepositoryMap;

    @Autowired
    public ListPrivateRegionShapesHandler(
        @Value("${private.agent.name}") String privateAgentName,
        Set<IRegionRepository<? extends IRegion<? extends ShapeConfig>>> regionRepos) {
        super(ListNativeShapesRequest.class, ActionType.LIST_PRIVATE_REGION_SHAPES);
        this.privateAgentName = privateAgentName;
        this.regionRepositoryMap = regionRepos.stream()
            .collect(Collectors.toMap(IRegionRepository::getCloud, Function.identity()));
    }

    @Override
    protected List<SdkPrivateAgentShapeInfo> handlePayload(ActionType action, ListNativeShapesRequest request) {
        if (!request.getPrivateAgentName().equals(privateAgentName)) {
            throw new IllegalArgumentException("Private agent name mismatch. Private agent name is " + privateAgentName +
                ", but name from request is " + request.getPrivateAgentName());
        }
        PrivateCloudType cloudType = PrivateCloudType.fromSdkCloud(request.getCloud());
        IRegionRepository<? extends IRegion<? extends ShapeConfig>> regionRepository = regionRepositoryMap.get(cloudType);
        if (regionRepository == null) {
            throw new IllegalArgumentException("Cloud " + request.getCloud() + " is not supported");
        }
        IRegion<? extends ShapeConfig> region = regionRepository.findByAliasInCloud(request.getNativeRegion());
        if (region == null) {
            throw new IllegalArgumentException("Region " + request.getNativeRegion() + " is not activated in cloud " + cloudType);
        }
        List<? extends ShapeConfig> allowedShapes = region.getAllowedShapes();
        if (allowedShapes == null) {
            return Collections.emptyList();
        }
        return allowedShapes.stream()
            .map(config -> new SdkPrivateAgentShapeInfo(
                config.getNameAlias(),
                config.getCpuCount(),
                config.getMemorySizeMb(),
                config.getDiskSizeMb()))
            .collect(Collectors.toList());
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return null;
    }
}
