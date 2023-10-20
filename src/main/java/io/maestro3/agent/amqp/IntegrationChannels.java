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


public interface IntegrationChannels {

    interface Inbound {
        String PLAIN = "inboudChannel";
        String ZIP = "zipChannel";
        String JSON = "jsonChannel";
        String DISPATCHER = "dispatcher";
        String PRIVATE_CLOUD = "privateCloudChannel";

        // OpenStack
        String OS_BYTES = "in.open_stack.bytes.channel";
        String OS_NOTIFICATIONS = "in.open_stack.notifications.channel";
        String OS_NOTIFICATIONS_ENCODED = "in.open_stack.notifications.encoded.channel";
    }

    interface Outbound {
        String PLAIN = "outboundChannel";
        String JSON = "outboundJsonChannel";
        String ZIP = "outboundZipChannel";
    }
}
