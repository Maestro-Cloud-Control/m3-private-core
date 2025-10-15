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

package io.maestro3.agent.platform.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

public class PlatformServiceDeploymentInfo {

    @Field(Fields.PROVIDER_TYPE)
    @JsonProperty(Fields.PROVIDER_TYPE)
    private PlatformServiceDeploymentType providerType;
    @Field(Fields.PROVIDER_PARAMS)
    @JsonProperty(Fields.PROVIDER_PARAMS)
    private Map<String, String> providerParams;

    public PlatformServiceDeploymentType getProviderType() {
        return providerType;
    }

    public PlatformServiceDeploymentInfo withProviderType(PlatformServiceDeploymentType providerType) {
        this.providerType = providerType;
        return this;
    }

    public Map<String, String> getProviderParams() {
        return providerParams;
    }

    public PlatformServiceDeploymentInfo withProviderParams(Map<String, String> providerParams) {
        this.providerParams = providerParams;
        return this;
    }

    public static final class Fields {
        public static final String PROVIDER_TYPE = "providerType";
        public static final String PROVIDER_PARAMS = "providerParams";

        // terraform provider params
        public static final String TENANT_DISPLAY_NAME = "tenantDisplayName";
        public static final String REGION_NAME = "regionName";
        public static final String CLOUD = "cloud";
        public static final String TEMPLATE_NAME = "templateName";
        public static final String TASK_ID = "taskId";
        public static final String TASK_NAME = "taskName";

        // private agent provider params
        public static final String PRIVATE_AGENT_ID = "privateAgentId";
        public static final String SERVICE_VARIABLES = "serviceVariables";
        public static final String PRIVATE_AGENT_SERVICE_ENTRY_ID = "agentServiceEntryId";

        private Fields() {
            throw new UnsupportedOperationException("Class is not designed for an instantiation");
        }
    }
}
