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

package io.maestro3.agent.scheduler;

import io.maestro3.agent.dao.LockDao;
import io.maestro3.agent.model.base.Lock;
import io.maestro3.agent.model.base.PrivateCloudType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


public abstract class AbstractScheduler implements IScheduler {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private long lastExecutionStart;
    private long lastExecutionEnd;
    private PrivateCloudType privateCloudType;
    private LockDao lockDao;
    private boolean lockingEnabled;
    @Value("${private.agent.lock.disabled}")
    private boolean disableAllLocks;

    public AbstractScheduler(PrivateCloudType privateCloudType, boolean lockingEnabled) {
        this.privateCloudType = privateCloudType;
        this.lockingEnabled = lockingEnabled;
    }

    protected void start(String msg) {
        LOG.info(msg);
        this.lastExecutionStart = System.currentTimeMillis();
    }

    protected void end(String msg) {
        LOG.info(msg);
        this.lastExecutionEnd = System.currentTimeMillis();
    }

    public void executeSchedule() {
        if (lockingEnabled && !disableAllLocks) {
            if (!lockDao.save(new Lock(privateCloudType.name(), getScheduleTitle()))) {
                LOG.debug("Another scheduler already executed for cloud {}", privateCloudType);
                return;
            }
        }
        try {
            execute();
        } finally {
            if (lockingEnabled && !disableAllLocks) {
                lockDao.delete(privateCloudType, getScheduleTitle());
            }
        }
    }

    protected abstract void execute();

    @Autowired
    public void setLockDao(LockDao lockDao) {
        this.lockDao = lockDao;
    }

    @Override
    public long getLastExecutionStart() {
        return lastExecutionStart;
    }

    @Override
    public long getLastExecutionEnd() {
        return lastExecutionEnd;
    }
}
