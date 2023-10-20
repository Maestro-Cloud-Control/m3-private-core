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
import io.maestro3.agent.dao.ITenantRepository;
import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.util.conversion.ConversionUtils;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.SdkPrivateAgentRegion;
import io.maestro3.sdk.v3.request.agent.ListAvailableRegionsForTenantRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ListPrivateRegionsHandler extends AbstractM3ApiHandler<ListAvailableRegionsForTenantRequest, List<SdkPrivateAgentRegion>> {

    private String privateAgentName;
    private Map<PrivateCloudType, ITenantRepository<? extends ITenant>> tenantRepositoryMap;
    private Map<PrivateCloudType, IRegionRepository<? extends IRegion>> regionRepositoryMap;
    private String exchangeName;
    private String syncQueue;
    private String asyncQueue;
    private String responseQueue;
    private String keyName;


    @Autowired
    public ListPrivateRegionsHandler(
        @Value("${private.agent.rabbit.m3api.exchange}") String exchangeName,
        @Value("${private.agent.rabbit.m3api.sync.queue}") String syncQueue,
        @Value("${private.agent.rabbit.m3api.async.queue}") String asyncQueue,
        @Value("${server.m3api.response.queue.name}") String responseQueue,
        @Value("${server.m3api.access.key}") String keyName,
        @Value("${private.agent.name}") String privateAgentName,
        Set<ITenantRepository<? extends ITenant>> tenantRepos,
        Set<IRegionRepository<? extends IRegion>> regionRepos) {
        super(ListAvailableRegionsForTenantRequest.class, ActionType.LIST_PRIVATE_AGENT_REGIONS);
        this.exchangeName = exchangeName;
        this.privateAgentName = privateAgentName;
        this.syncQueue = syncQueue;
        this.asyncQueue = asyncQueue;
        this.responseQueue = responseQueue;
        this.keyName = keyName;
        this.tenantRepositoryMap = tenantRepos.stream()
            .collect(Collectors.toMap(ITenantRepository::getCloud, Function.identity()));
        this.regionRepositoryMap = regionRepos.stream()
            .collect(Collectors.toMap(IRegionRepository::getCloud, Function.identity()));
    }

    @Override
    protected List<SdkPrivateAgentRegion> handlePayload(ActionType action, ListAvailableRegionsForTenantRequest request) {
        if (!request.getPrivateAgentName().equals(privateAgentName)) {
            throw new IllegalArgumentException("Private agent name mismatch. Private agent name is " + privateAgentName +
                ", but name from request is " + request.getPrivateAgentName());
        }
        IRegionRepository<? extends IRegion> regionRepository =
            regionRepositoryMap.get(PrivateCloudType.fromSdkCloud(request.getCloud()));
        ITenantRepository<? extends ITenant> tenantRepository =
            tenantRepositoryMap.get(PrivateCloudType.fromSdkCloud(request.getCloud()));

        Map<String, IRegion> regionMap = regionRepository.findAllRegionsForCloud().stream()
            .collect(Collectors.toMap(IRegion::getId, Function.identity()));
        List<? extends ITenant> tenantsForCloud = tenantRepository.findAllInCloud();
        return tenantsForCloud.stream()
            .filter(tenant -> tenant.getTenantAlias().equals(request.getTenantName()))
            .map(tenant -> ConversionUtils.toSdkRegion(regionMap.get(tenant.getRegionId()), keyName, privateAgentName,
                syncQueue, asyncQueue, exchangeName, responseQueue))
            .collect(Collectors.toList());
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return null;
    }
}
