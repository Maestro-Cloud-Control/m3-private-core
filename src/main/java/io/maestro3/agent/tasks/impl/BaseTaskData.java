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

package io.maestro3.agent.tasks.impl;

import io.maestro3.agent.tasks.ITaskData;

import java.util.Objects;


public class BaseTaskData implements ITaskData {

    private String id;
    private String regionId;
    private String tenantId;
    private int counter;
    private Runnable task;
    private long creationTime = System.currentTimeMillis();
    private long executionTime = System.currentTimeMillis();

    public BaseTaskData(String id, String tenantId, String regionId, Runnable task) {
        this.id = id;
        this.task = task;
        this.tenantId = tenantId;
        this.regionId = regionId;
    }

    public BaseTaskData(String id, String regionId, String tenantId, Runnable task, long executionTime) {
        this(id, tenantId, regionId, task);
        this.executionTime = executionTime;
    }

    @Override
    public Runnable getTask() {
        return task;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getRegionId() {
        return regionId;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public int getCounter() {
        return counter;
    }

    @Override
    public void increaseCounterByOne() {
        this.counter += 1;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseTaskData taskData = (BaseTaskData) o;
        return Objects.equals(id, taskData.id) &&
            Objects.equals(regionId, taskData.regionId) &&
            Objects.equals(tenantId, taskData.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, regionId, tenantId);
    }
}
