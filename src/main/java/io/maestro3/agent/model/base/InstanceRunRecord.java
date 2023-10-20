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


public class InstanceRunRecord {
    private String region;
    private String tenant;
    private String imageId;
    private long startDuration;

    public InstanceRunRecord(String region, String tenant, String imageId, long startDuration) {
        this.region = region;
        this.tenant = tenant;
        this.imageId = imageId;
        this.startDuration = startDuration;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public long getStartDuration() {
        return startDuration;
    }

    public void setStartDuration(long startDuration) {
        this.startDuration = startDuration;
    }
}
