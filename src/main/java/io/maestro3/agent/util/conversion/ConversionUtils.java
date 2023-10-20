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

package io.maestro3.agent.util.conversion;

import io.maestro3.agent.model.base.IRegion;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.SdkPrivateAgentRegion;

public final class ConversionUtils {

    private ConversionUtils() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }

    public static SdkPrivateAgentRegion toSdkRegion(IRegion region, String keyName, String agentName, String sync,
                                                    String async, String exchange, String response) {
        return new SdkPrivateAgentRegion()
            .setRegionNativeName(region.getRegionAlias())
            .setSdkKey(keyName)
            .setAgentName(agentName)
            .setAsyncQueue(async)
            .setSyncQueue(sync)
            .setCloud(SdkCloud.fromValue(region.getCloud().name()))
            .setExchangeName(exchange)
            .setResponseQueue(response)
            .setAvailable(true);
    }
}
