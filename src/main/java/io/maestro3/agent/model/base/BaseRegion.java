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

package io.maestro3.agent.model.base;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;


@CompoundIndex(unique = true, def = "{'regionAlias' : 1}" )
@Document("Regions")
public abstract class BaseRegion<SHAPE extends ShapeConfig> implements IRegion<SHAPE> {

    @Id
    private String id;
    @NotBlank
    private String regionAlias;
    private boolean managementAvailable;
    private List<SHAPE> allowedShapes = new ArrayList<>();
    private PrivateCloudType cloud;

    public BaseRegion() {
    }

    public BaseRegion(PrivateCloudType cloud) {
        this.cloud = cloud;
    }

    public PrivateCloudType getCloud() {
        return cloud;
    }

    public void setCloud(PrivateCloudType cloud) {
        this.cloud = cloud;
    }

    public List<SHAPE> getAllowedShapes() {
        return allowedShapes;
    }

    public void setAllowedShapes(List<SHAPE> allowedShapes) {
        this.allowedShapes = allowedShapes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegionAlias() {
        return regionAlias;
    }

    public void setRegionAlias(String regionAlias) {
        this.regionAlias = regionAlias;
    }

    public boolean isManagementAvailable() {
        return managementAvailable;
    }

    public void setManagementAvailable(boolean managementAvailable) {
        this.managementAvailable = managementAvailable;
    }
}
