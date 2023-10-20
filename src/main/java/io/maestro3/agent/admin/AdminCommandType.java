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

public enum AdminCommandType {

    //open_stack
    OPEN_STACK_CREATE_REGION,
    OPEN_STACK_SET_SERVICE_TENANT,
    OPEN_STACK_SET_USER,
    OPEN_STACK_CREATE_ADMIN_PROJECT_META,
    OPEN_STACK_ADD_PROJECT_META_IMAGE,
    OPEN_STACK_CREATE_IMAGE,
    OPEN_STACK_CREATE_SHAPE,
    OPEN_STACK_CREATE_TENANT,
    OPEN_STACK_CREATE_TENANT_USER,
    OPEN_STACK_SET_REGION_MANAGEMENT,
    OPEN_STACK_SET_TENANT_MANAGEMENT,
    OPEN_STACK_SET_TENANT_DESCRIBER_MODE,
    OPEN_STACK_CONFIGURE_TENANT_NETWORK,
}
