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

package io.maestro3.agent.api;

public final class ApiConstants {

    public static final String COMPUTE_SERVICE = "Compute";
    public static final String MACHINE_IMAGES = "Machine Images";
    public static final String VOLUME = "Volume";
    public static final String VIRT_MACHINE = "Virtual machine";
    public static final String VAPP_TYPE = "VApp";
    public static final String VAPP_TEMPLATE_TYPE = "VAppTemplate";
    public static final String NETWORK_TYPE = "Network";
    public static final String STORAGE_PROFILE_TYPE = "Storage Profile";

    public static final String SDK_VALIDATOR = "sdkValidator";
    public static final String API_VALIDATOR = "adminApiValidator";

    private ApiConstants() {
    }
}
