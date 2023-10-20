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

import io.maestro3.agent.admin.IAdminCommandFactory;
import io.maestro3.agent.service.IPrivateWizardProcessor;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import io.maestro3.sdk.v3.model.agent.wizard.SdkWizardAction;
import io.maestro3.sdk.v3.request.agent.PrivateWizardRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PrivateWizardHandlerHandler extends AbstractM3ApiHandler<PrivateWizardRequest, SdkPrivateWizard> {

    private final Map<String, IPrivateWizardProcessor> processorMap;
    private final Map<SdkWizardAction,
        BiFunction<IPrivateWizardProcessor, SdkPrivateWizard, SdkPrivateWizard>> actions = new HashMap<>();

    @Autowired
    public PrivateWizardHandlerHandler(IAdminCommandFactory adminCommandFactory, List<IPrivateWizardProcessor> processors) {
        super(PrivateWizardRequest.class, ActionType.WIZARD_ACTION);
        this.processorMap = processors.stream()
            .collect(Collectors.toMap(IPrivateWizardProcessor::type, Function.identity()));
        this.actions.put(SdkWizardAction.GET, IPrivateWizardProcessor::get);
        this.actions.put(SdkWizardAction.NEXT, IPrivateWizardProcessor::next);
        this.actions.put(SdkWizardAction.PREV, IPrivateWizardProcessor::prev);
        this.actions.put(SdkWizardAction.VERIFY, (processor, wizard) -> processor.verify(adminCommandFactory, wizard));
        this.actions.put(SdkWizardAction.EXECUTE, (processor, wizard) -> processor.execute(adminCommandFactory, wizard));
    }

    @Override
    protected SdkPrivateWizard handlePayload(ActionType action, PrivateWizardRequest request) {
        IPrivateWizardProcessor processor = processorMap.get(request.getWizardType());
        if (processor == null) {
            throw new IllegalArgumentException("Not found processor for type " + request.getWizardType());
        }
        return actions.get(request.getAction()).apply(processor, request.getWizard());
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return null;
    }
}
