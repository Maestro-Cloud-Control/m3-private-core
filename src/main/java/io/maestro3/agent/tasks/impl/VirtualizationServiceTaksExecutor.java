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
import io.maestro3.agent.tasks.ITaskExecutor;
import io.maestro3.agent.tasks.exception.ExpectedTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


@Component
public class VirtualizationServiceTaksExecutor implements ITaskExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualizationServiceTaksExecutor.class);

    private HashSet<ITaskData> taskQueue;

    private int retryCount;
    private long maxTaskTtl;

    @Autowired
    public VirtualizationServiceTaksExecutor(@Value("${internal.task.retry.count}") int retryCount,
                                             @Value("${internal.task.retry.ttl}") long maxTaskTtlMin) {
        this.retryCount = retryCount;
        this.maxTaskTtl = maxTaskTtlMin * 60_000;
        this.taskQueue = new HashSet<>();
    }

    @Override
    public void addTask(ITaskData data) {
        if (taskQueue.contains(data)) {
            return;
        }
        taskQueue.add(data);
    }

    @Scheduled(cron = "${internal.task.retry.cron}")
    public void process() {
        Set<ITaskData> availableTasks = taskQueue;
        this.taskQueue = new HashSet<>();
        for (ITaskData data : availableTasks) {
            try {
                if (data.getExecutionTime() > System.currentTimeMillis()) {
                    this.taskQueue.add(data);
                    continue;
                }
                if (data.getCreationTime() + maxTaskTtl < System.currentTimeMillis()
                    || data.getCounter() > retryCount) {
                    continue;
                }
                data.getTask().run();
            } catch (ExpectedTaskException ex) {
                this.taskQueue.add(data);
                LOGGER.warn("Failed to execute task for {} reason: {}", data.getId(), ex.getMessage());
            } catch (Exception ex) {
                data.increaseCounterByOne();
                this.taskQueue.add(data);
                LOGGER.error("Failed to execute task for {} reason: {}", data.getId(), ex.getMessage(), ex);
            }
        }
    }
}
