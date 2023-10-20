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
import io.maestro3.agent.model.base.ShapeConfig;
import io.maestro3.agent.service.IInstanceChecker;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.request.agent.ResendInstanceAuditRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ResendInstanceAuditHandler extends AbstractM3ApiHandler<ResendInstanceAuditRequest, Void> {

    private Map<PrivateCloudType, IInstanceChecker> checkerMap;
    private Map<PrivateCloudType, ITenantRepository<? extends ITenant>> tenantRepositoryMap;
    private Map<PrivateCloudType, IRegionRepository<? extends IRegion<? extends ShapeConfig>>> regionRepositoryMap;

    @Autowired
    public ResendInstanceAuditHandler(Set<IInstanceChecker> checkers,
                                      Set<ITenantRepository<? extends ITenant>> tenantRepos,
                                      Set<IRegionRepository<? extends IRegion<? extends ShapeConfig>>> regionRepos) {
        super(ResendInstanceAuditRequest.class, ActionType.RESEND_INSTANCE_AUDIT);
        this.checkerMap = checkers.stream()
            .collect(Collectors.toMap(IInstanceChecker::getCloud, Function.identity()));
        this.tenantRepositoryMap = tenantRepos.stream()
            .collect(Collectors.toMap(ITenantRepository::getCloud, Function.identity()));
        this.regionRepositoryMap = regionRepos.stream()
            .collect(Collectors.toMap(IRegionRepository::getCloud, Function.identity()));
    }

    @Override
    protected Void handlePayload(ActionType action, ResendInstanceAuditRequest request) {
        PrivateCloudType privateCloudType = PrivateCloudType.fromSdkCloud(request.getCloud());
        IRegionRepository<? extends IRegion> regionRepository =
            regionRepositoryMap.get(privateCloudType);
        ITenantRepository<? extends ITenant> tenantRepository =
            tenantRepositoryMap.get(privateCloudType);
        IRegion region = regionRepository.findByAliasInCloud(request.getRegionName());
        if (region == null) {
            throw new IllegalArgumentException("Region " + request.getRegionName() + " is not activated in cloud " + privateCloudType);
        }
        ITenant tenant = tenantRepository.findByTenantAliasAndRegionIdInCloud(request.getTenantName(), region.getId());
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant " + request.getTenantName() + " is not activated in region " + request.getRegionName());
        }
        IInstanceChecker instanceChecker = checkerMap.get(privateCloudType);
        if (instanceChecker == null) {
            throw new IllegalArgumentException("Audit resend is not available for " + privateCloudType + " cloud");
        }
        instanceChecker.checkInstances(tenant, region, request.getDataList());
        return null;
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return null;
    }
}
