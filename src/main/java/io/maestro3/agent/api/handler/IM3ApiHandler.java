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

package io.maestro3.agent.api.handler;

import io.maestro3.sdk.M3SdkVersion;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.ResultStatus;
import io.maestro3.sdk.v3.model.SdkCloud;

import java.util.Set;

public interface IM3ApiHandler {

    /**
     * Get supported by handler sdk version
     *
     * @return supported sdk version
     */
    M3SdkVersion getSupportedVersion();

    /**
     * Get the list of supported action types.
     *
     * @return the list of supported action types.
     */
    Set<ActionType> getSupportedActions();

    /**
     * Process the given request.
     * <br/>
     * <b>Note:</b> All the exceptions should be caught and returned within the result with ResultStatus.FAILED status
     * rather then throw Runtime exception.
     *
     * @param request the request to be processed.
     * @return the result of request processing
     * @see M3RawResult
     * @see M3ApiAction
     * @see ResultStatus
     */
    M3RawResult handle(M3ApiAction request);

    SdkCloud getSupportedCloud();
}
