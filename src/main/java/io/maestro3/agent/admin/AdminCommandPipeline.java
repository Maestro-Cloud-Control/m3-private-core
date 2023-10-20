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

package io.maestro3.agent.admin;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;
import java.util.List;

public enum AdminCommandPipeline {

    CONFIGURE_OPEN_STACK_REGION("configure_open_stack_region", Arrays.asList(
        AdminCommandType.OPEN_STACK_CREATE_REGION,
        AdminCommandType.OPEN_STACK_CREATE_ADMIN_PROJECT_META,
        AdminCommandType.OPEN_STACK_ADD_PROJECT_META_IMAGE,
        AdminCommandType.OPEN_STACK_CREATE_IMAGE,
        AdminCommandType.OPEN_STACK_CREATE_SHAPE,
        AdminCommandType.OPEN_STACK_SET_REGION_MANAGEMENT
    )),
    CONFIGURE_OPEN_STACK_TENANT("activate_open_stack_tenant", Arrays.asList(
        AdminCommandType.OPEN_STACK_CREATE_TENANT,
        AdminCommandType.OPEN_STACK_CREATE_TENANT_USER,
        AdminCommandType.OPEN_STACK_SET_TENANT_DESCRIBER_MODE,
        AdminCommandType.OPEN_STACK_SET_TENANT_MANAGEMENT
    ));

    private final String name;
    private final List<AdminCommandType> commandTypes;

    AdminCommandPipeline(String name, List<AdminCommandType> commandTypes) {
        this.name = name;
        this.commandTypes = commandTypes;
    }

    public String getName() {
        return name;
    }

    public List<AdminCommandType> getCommandTypes() {
        return commandTypes;
    }

    @JsonCreator
    public static AdminCommandPipeline fromValue(String name) {
        for (AdminCommandPipeline pipeline : values()) {
            if (pipeline.name.equalsIgnoreCase(name)) {
                return pipeline;
            }
        }

        throw new IllegalStateException("Can't instantiate AdminCommandPipeline by name: " + name);
    }
}
