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
import io.maestro3.sdk.v3.model.instance.SdkVolume;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class AbstractVolumeChecker<TENANT extends ITenant, REGION extends IRegion, VOLUME>
    implements IVolumesChecker<TENANT, REGION> {

    private PrivateCloudType cloud;

    protected ICadfAuditEventSender auditEventSender;


    protected AbstractVolumeChecker(PrivateCloudType cloud, ICadfAuditEventSender auditEventSender) {
        this.cloud = cloud;
        this.auditEventSender = auditEventSender;
    }

    @Override
    public void checkVolumes(TENANT tenant, REGION region, List<SdkVolume> volumes) {
        Map<String, VOLUME> actualVolumes = getVolumesFromDb(tenant, region);
        Set<String> processedVolumes = new HashSet<>();
        List<Pair<VOLUME, SdkVolume>> newVolumes;
        List<Pair<VOLUME, SdkVolume>> changeStateVolumes = new ArrayList<>();
        List<Pair<VOLUME, SdkVolume>> configurationChangedVolumes = new ArrayList<>();
        List<Pair<VOLUME, SdkVolume>> deletedVolumes = new ArrayList<>();
        for (SdkVolume volume : volumes) {
            if (processedVolumes.contains(volume.getVolumeId())) {
                continue;
            }
            VOLUME volumeFromProvider = actualVolumes.remove(volume.getVolumeId());
            if (volumeFromProvider == null) {
                deletedVolumes.add(Pair.of(null, volume));
            } else {
                if (isVolumeStatusChanged(volume, volumeFromProvider)) {
                    changeStateVolumes.add(Pair.of(volumeFromProvider, volume));
                } else {
                    configurationChangedVolumes.add(Pair.of(volumeFromProvider, volume));
                }
                getUniqueIdKeys(volumeFromProvider)
                    .forEach(actualVolumes::remove);
            }
            processedVolumes.add(volume.getVolumeId());
        }
        newVolumes = collectNewVolumes(actualVolumes);
        if (CollectionUtils.isNotEmpty(newVolumes)) {
            processNewVolumes(tenant, region, newVolumes);
        }
        if (CollectionUtils.isNotEmpty(deletedVolumes)) {
            processDeletedVolumes(tenant, region, deletedVolumes);
        }
        if (CollectionUtils.isNotEmpty(changeStateVolumes)) {
            processStateChangedVolumes(tenant, region, changeStateVolumes);
        }
        if (CollectionUtils.isNotEmpty(configurationChangedVolumes)) {
            processConfigChangedVolumes(tenant, region, configurationChangedVolumes);
        }
    }

    private List<Pair<VOLUME, SdkVolume>> collectNewVolumes(Map<String, VOLUME> actualVolumes) {
        return actualVolumes.values().stream()
            .map(VOLUME -> getUniqueIdKeys(VOLUME).stream().findFirst().get())
            .distinct()
            .map(actualVolumes::get)
            .map(VOLUME -> Pair.<VOLUME, SdkVolume>of(VOLUME, null))
            .collect(Collectors.toList());
    }

    protected abstract List<String> getUniqueIdKeys(VOLUME VOLUME);

    protected abstract void processNewVolumes(TENANT tenant, REGION region, List<Pair<VOLUME, SdkVolume>> newVolume);

    protected abstract void processDeletedVolumes(TENANT tenant, REGION region, List<Pair<VOLUME, SdkVolume>> newVolume);

    protected abstract void processStateChangedVolumes(TENANT tenant, REGION region, List<Pair<VOLUME, SdkVolume>> newVolume);

    protected abstract void processConfigChangedVolumes(TENANT tenant, REGION region, List<Pair<VOLUME, SdkVolume>> newVolume);

    protected abstract boolean isVolumeStatusChanged(SdkVolume sdkVolume, VOLUME actualVOLUME);

    protected abstract Map<String, VOLUME> getVolumesFromDb(TENANT tenant, REGION region);

    @Override
    public PrivateCloudType getCloud() {
        return cloud;
    }
}
