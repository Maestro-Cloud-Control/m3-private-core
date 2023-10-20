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

package io.maestro3.agent.amqp.model;

public class SdkRabbitConfiguration extends SimpleRabbitConfiguration{
    private String syncQueue;
    private String asyncQueue;
    private String responseQueue;

    public SdkRabbitConfiguration(String syncQueue, String asyncQueue, String responseQueue, String host,
                                  String userName, String password, String virtualHost, int port) {
        super(host, userName, password, virtualHost, port);
        this.syncQueue = syncQueue;
        this.asyncQueue = asyncQueue;
        this.responseQueue = responseQueue;
    }


    public String getSyncQueue() {
        return syncQueue;
    }

    public void setSyncQueue(String syncQueue) {
        this.syncQueue = syncQueue;
    }

    public String getAsyncQueue() {
        return asyncQueue;
    }

    public void setAsyncQueue(String asyncQueue) {
        this.asyncQueue = asyncQueue;
    }

    public String getResponseQueue() {
        return responseQueue;
    }

    public void setResponseQueue(String responseQueue) {
        this.responseQueue = responseQueue;
    }
}
