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

import javax.validation.constraints.NotBlank;


public abstract class ShapeConfig {

    @NotBlank
    private String nameAlias;
    private int cpuCount;
    private long diskSizeMb;
    private long memorySizeMb;

    public ShapeConfig() {
    }

    public ShapeConfig(@NotBlank String nameAlias, int cpuCount, long diskSizeMb, long memorySizeMb) {
        this.nameAlias = nameAlias;
        this.cpuCount = cpuCount;
        this.diskSizeMb = diskSizeMb;
        this.memorySizeMb = memorySizeMb;
    }

    public String getNameAlias() {
        return nameAlias;
    }

    public void setNameAlias(String nameAlias) {
        this.nameAlias = nameAlias;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public long getDiskSizeMb() {
        return diskSizeMb;
    }

    public void setDiskSizeMb(long diskSizeMb) {
        this.diskSizeMb = diskSizeMb;
    }

    public long getMemorySizeMb() {
        return memorySizeMb;
    }

    public void setMemorySizeMb(long memorySizeMb) {
        this.memorySizeMb = memorySizeMb;
    }
}
