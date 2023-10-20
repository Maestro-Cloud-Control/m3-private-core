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

import io.maestro3.agent.util.IPrivateAgentStateUpdater;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PrivateStateHandlerHandler extends AbstractM3ApiHandler<Object, Map> {

    private final IPrivateAgentStateUpdater stateUpdater;

    @Autowired
    public PrivateStateHandlerHandler(IPrivateAgentStateUpdater stateUpdater) {
        super(Object.class, ActionType.GET_AGENT_STATE_ACTION);
        this.stateUpdater = stateUpdater;
    }

    @Override
    protected Map handlePayload(ActionType action, Object request) {
        return stateUpdater.getState();
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return null;
    }
}
