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

import com.fasterxml.jackson.core.type.TypeReference;
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.sdk.M3SdkVersion;
import io.maestro3.sdk.internal.util.JsonUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractM3ApiHandler<REQ, RES> implements IM3ApiHandler {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private final TypeReference<REQ> requestType;
    private final Set<ActionType> actionTypes;

    public AbstractM3ApiHandler(Class<REQ> requestType, ActionType supportedAction, ActionType... supportedActions) {
        this.actionTypes = EnumSet.of(supportedAction, supportedActions);
        this.requestType = new TypeReference<REQ>() {
            @Override
            public Type getType() {
                return requestType;
            }
        };
    }

    @Override
    public final M3SdkVersion getSupportedVersion() {
        return M3SdkVersion.V3;
    }

    @Override
    public final M3Result<RES> handle(M3ApiAction action) {
        String actionId = action.getId();
        Map<String, Object> params = action.getParams();
        MDC.put("action_id", actionId);
        LOG.info("Received action with id: {} and type {}", actionId, action.getType());

        if (!isEnabled()) {
            return M3Result.error(actionId, "Feature is not available");
        }

        LOG.debug("Request params: {}", params);

        try {
            RES resultModel = handlePayload(action);
            String resultJson = JsonUtils.convertObjectToJson(resultModel);
            return M3Result.success(actionId, resultJson, resultModel);
        } catch (ReadableAgentException e) {
            LOG.error("Cannot execute action", e);
            return M3Result.error(actionId, e.getMessage(), e.getMessage());
        } catch (Exception e) {
            LOG.error("Cannot execute action", e);
            return M3Result.error(actionId, e.getMessage());
        }
    }

    @Override
    public final Set<ActionType> getSupportedActions() {
        return actionTypes;
    }

    private RES handlePayload(M3ApiAction apiAction) throws Exception {
        String body = (String) apiAction.getParams().get("body");
        if (StringUtils.isBlank(body) || Void.TYPE.equals(requestType.getType())) {
            return handlePayload(apiAction, null);
        }
        REQ request = JsonUtils.parseJson(body, requestType);
        LOG.debug("Typed request params: {}", request);

        return handlePayload(apiAction, request);
    }

    protected RES handlePayload(M3ApiAction apiAction, REQ request) throws Exception {
        return handlePayload(apiAction.getType(), request);
    }

    protected abstract RES handlePayload(ActionType action, REQ request) throws Exception;

    protected boolean isEnabled() {
        return true;
    }

}
