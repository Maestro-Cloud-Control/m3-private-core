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

import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;

public class SimpleRabbitConfiguration implements IRabbitConfiguration {
    @NotBlank
    protected String rabbitHost;
    @NotBlank
    protected String rabbitUsername;
    @NotBlank
    protected String rabbitPassword;
    @NotBlank
    protected String rabbitVirtHost;
    @Range
    protected int rabbitPort;

    public SimpleRabbitConfiguration() {
    }

    public SimpleRabbitConfiguration(String rabbitHost, String rabbitUsername, String rabbitPassword, String rabbitVirtHost, int rabbitPort) {
        this.rabbitHost = rabbitHost;
        this.rabbitUsername = rabbitUsername;
        this.rabbitPassword = rabbitPassword;
        this.rabbitVirtHost = rabbitVirtHost;
        this.rabbitPort = rabbitPort;
    }

    @Override
    public String getRabbitHost() {
        return rabbitHost;
    }

    public void setRabbitHost(String rabbitHost) {
        this.rabbitHost = rabbitHost;
    }

    @Override
    public String getRabbitUsername() {
        return rabbitUsername;
    }

    public void setRabbitUsername(String rabbitUsername) {
        this.rabbitUsername = rabbitUsername;
    }

    @Override
    public String getRabbitPassword() {
        return rabbitPassword;
    }

    public void setRabbitPassword(String rabbitPassword) {
        this.rabbitPassword = rabbitPassword;
    }

    @Override
    public String getRabbitVirtHost() {
        return rabbitVirtHost;
    }

    public void setRabbitVirtHost(String rabbitVirtHost) {
        this.rabbitVirtHost = rabbitVirtHost;
    }

    @Override
    public int getRabbitPort() {
        return rabbitPort;
    }

    public void setRabbitPort(int rabbitPort) {
        this.rabbitPort = rabbitPort;
    }
}
