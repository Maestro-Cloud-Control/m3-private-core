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


public class Lock {

    @Id
    private String cloud;
    private String lockName;
    private long lockDate;

    public Lock() {
    }

    public Lock(String cloud, String lockName) {
        this.cloud = cloud;
        this.lockName = lockName;
        this.lockDate = System.currentTimeMillis();
    }

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public long getLockDate() {
        return lockDate;
    }

    public void setLockDate(long lockDate) {
        this.lockDate = lockDate;
    }
}
