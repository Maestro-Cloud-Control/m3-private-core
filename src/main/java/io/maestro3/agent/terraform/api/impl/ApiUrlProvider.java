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

package io.maestro3.agent.terraform.api.impl;

import io.maestro3.agent.terraform.api.IApiUrlProvider;
import io.maestro3.agent.terraform.model.TerraformTemplate;
import io.maestro3.chef.service.ISecretsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApiUrlProvider implements IApiUrlProvider {

    private static final String CALLBACK_URL_PATTERN = "%s%s%s?%s=%s";
    private static final String API_HOST_SETTING = "API_HOST";
    private static final String TERRAFORM_WEBHOOK_CONTROLLER_SUFFIX = "webhook";
    private static final String TEMPLATE_ID = "templateId";

    private final ISecretsService settingService;
    private final String apiUrlPrefix;

    @Autowired
    public ApiUrlProvider(ISecretsService settingService) {
        this.settingService = settingService;
        this.apiUrlPrefix = "/test";
    }

    @Override
    public String generateWebhookCallbackUrl(final TerraformTemplate terraformTemplate) {
        Optional<String> apiHost = Optional.ofNullable(settingService.getSecretValue(API_HOST_SETTING));
        return apiHost.map(host -> String.format(CALLBACK_URL_PATTERN, host, apiUrlPrefix, TERRAFORM_WEBHOOK_CONTROLLER_SUFFIX,
                        TEMPLATE_ID, terraformTemplate.getTemplateId()))
                .orElseThrow(() -> new IllegalStateException("API host setting not found"));
    }
}
