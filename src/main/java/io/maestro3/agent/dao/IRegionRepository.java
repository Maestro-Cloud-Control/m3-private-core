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

package io.maestro3.agent.dao;

import io.maestro3.agent.model.base.IAmqpSupportedRegion;
import io.maestro3.agent.model.base.IRegion;
import io.maestro3.agent.model.base.PrivateCloudType;

import java.util.List;


public interface IRegionRepository<REGION extends IRegion> {

    void delete(REGION cloud);

    void save(REGION cloud);

    List<IRegion> findAll();

    List<REGION> findAllRegionsForCloud();

    IRegion findById (String id);

    IRegion findByRegionAlias(String regionAlias);

    REGION findByIdInCloud(String id);

    REGION findByAliasInCloud(String alias);

    List<IAmqpSupportedRegion> findAllAmqpRegions();

    List<IRegion> findByCloud(String cloudName);

    PrivateCloudType getCloud();
}
