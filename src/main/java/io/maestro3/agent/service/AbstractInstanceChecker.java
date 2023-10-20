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

package io.maestro3.agent.service;

import io.maestro3.agent.cadf.ICadfAuditEventSender;
import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.v3.model.instance.SdkInstance;
import io.maestro3.sdk.v3.model.instance.SdkInstanceState;
import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfActions;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class AbstractInstanceChecker<TENANT extends ITenant, REGION extends IRegion, VM>
    implements IInstanceChecker<TENANT, REGION> {

    private PrivateCloudType cloud;

    protected ICadfAuditEventSender auditEventSender;


    protected AbstractInstanceChecker(PrivateCloudType cloud, ICadfAuditEventSender auditEventSender) {
        this.cloud = cloud;
        this.auditEventSender = auditEventSender;
    }

    @Override
    public void checkInstances(TENANT tenant, REGION region, List<SdkInstance> instances) {
        Map<String, VM> actualVms = getVmsFromDb(tenant, region);
        Set<String> processedVms = new HashSet<>();
        List<Pair<VM, SdkInstance>> newInstances;
        List<Pair<VM, SdkInstance>> changeStateInstances = new ArrayList<>();
        List<Pair<VM, SdkInstance>> configurationChangedInstances = new ArrayList<>();
        List<Pair<VM, SdkInstance>> terminatedInstances = new ArrayList<>();
        for (SdkInstance instance : instances) {
            if (processedVms.contains(instance.getInstanceId())) {
                continue;
            }
            VM vMFromProvider = actualVms.remove(instance.getInstanceId());
            if (vMFromProvider == null) {
                terminatedInstances.add(Pair.of(null, instance));
            } else {
                if (isVmStatusChanged(instance, vMFromProvider)) {
                    changeStateInstances.add(Pair.of(vMFromProvider, instance));
                } else {
                    configurationChangedInstances.add(Pair.of(vMFromProvider, instance));
                }
                getUniqueIdKeys(vMFromProvider)
                    .forEach(actualVms::remove);
            }
            processedVms.add(instance.getInstanceId());
        }
        newInstances = collectNewInstances(actualVms);
        if (CollectionUtils.isNotEmpty(newInstances)) {
            processNewInstances(tenant, region, newInstances);
        }
        if (CollectionUtils.isNotEmpty(terminatedInstances)) {
            processTerminatedInstances(tenant, region, terminatedInstances);
        }
        if (CollectionUtils.isNotEmpty(changeStateInstances)) {
            processStateChangedInstances(tenant, region, changeStateInstances);
        }
        if (CollectionUtils.isNotEmpty(configurationChangedInstances)) {
            processConfigChangedInstances(tenant, region, configurationChangedInstances);
        }
    }

    private List<Pair<VM, SdkInstance>> collectNewInstances(Map<String, VM> actualVms) {
        return actualVms.values().stream()
            .map(vm -> getUniqueIdKeys(vm).stream().findFirst().get())
            .distinct()
            .map(actualVms::get)
            .map(vm -> Pair.<VM, SdkInstance>of(vm, null))
            .collect(Collectors.toList());
    }

    protected abstract List<String> getUniqueIdKeys(VM vm);

    protected abstract void processNewInstances(TENANT tenant, REGION region, List<Pair<VM, SdkInstance>> newInstances);

    protected abstract void processTerminatedInstances(TENANT tenant, REGION region, List<Pair<VM, SdkInstance>> newInstances);

    protected abstract void processStateChangedInstances(TENANT tenant, REGION region, List<Pair<VM, SdkInstance>> newInstances);

    protected abstract void processConfigChangedInstances(TENANT tenant, REGION region, List<Pair<VM, SdkInstance>> newInstances);

    protected abstract boolean isVmStatusChanged(SdkInstance sdkInstance, VM actualVm);

    protected abstract Map<String, VM> getVmsFromDb(TENANT tenant, REGION region);

    protected ICadfAction getPossibleActionFromState(SdkInstanceState status) {
        switch (status) {
            case RUNNING:
                return CadfActions.create();
            case STOPPED:
                return CadfActions.stop();
            case REBOOTED:
                return CadfActions.start();
            case SUSPENDED:
                return CadfActions.stop();
            case TERMINATED:
                return CadfActions.terminated();
            case MISSING:
                return CadfActions.terminated();
            case ERROR:
                return CadfActions.terminated();
            case UNKNOWN:
                return CadfActions.terminated();
            default:
                return null;
        }
    }

    @Override
    public PrivateCloudType getCloud() {
        return cloud;
    }
}
