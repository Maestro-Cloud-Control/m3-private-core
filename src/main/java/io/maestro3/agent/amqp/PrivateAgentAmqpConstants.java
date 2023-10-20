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

package io.maestro3.agent.amqp;

public final class PrivateAgentAmqpConstants {
    public static final String REGION_ID = "region_id";
    public static final String TEMP_ID = "temp_region";
    public static final String SDK_REGION = "m3_sdk_region";

    private PrivateAgentAmqpConstants() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }
}
