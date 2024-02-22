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

package io.maestro3.agent.chef.service;

import io.maestro3.agent.dao.IRegionRepository;
import io.maestro3.agent.model.base.IRegion;
import io.maestro3.chef.service.IRegionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Serhii Akhmetshin
 * Created: 19/12/2023
 */
@Service
public class RegionProvider implements IRegionProvider {
    private IRegionRepository regionRepository;

    @Autowired
    public RegionProvider(List<IRegionRepository> regionRepositories) {
        this.regionRepository = regionRepositories.get(0);
    }

    @Override
    public List<String> findAll() {
        List<IRegion> all = regionRepository.findAll();
        return all.stream()
                .map(IRegion::getRegionAlias)
                .collect(Collectors.toList());
    }

    @Override
    public String findById(String s) {
        IRegion byId = regionRepository.findById(s);
        return byId == null ? null : byId.getRegionAlias();
    }
}
