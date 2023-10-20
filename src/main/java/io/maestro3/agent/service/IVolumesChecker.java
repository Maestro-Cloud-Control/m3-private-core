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

import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.ITenant;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.sdk.v3.model.instance.SdkInstance;
import io.maestro3.sdk.v3.model.instance.SdkVolume;

import java.util.List;


public interface IVolumesChecker<TENANT extends ITenant, REGION extends IRegion> {

    void checkVolumes(TENANT tenant, REGION region, List<SdkVolume> instances);

    PrivateCloudType getCloud();

}
