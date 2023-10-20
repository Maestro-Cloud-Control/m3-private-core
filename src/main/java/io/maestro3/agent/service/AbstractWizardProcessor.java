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

package io.maestro3.agent.service;

import io.maestro3.agent.service.step.IPrivateStepProcessor;
import io.maestro3.agent.util.PrivateWizardUtils;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStep;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStepData;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractWizardProcessor<PROCESSOR extends IPrivateStepProcessor> implements IPrivateWizardProcessor {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final Map<Integer, PROCESSOR> stepProcessors;
    private final String type;

    public AbstractWizardProcessor(List<PROCESSOR> stepProcessors, String type) {
        this.type = type;
        this.stepProcessors = stepProcessors.stream()
            .filter(p -> p.getWizardType().equals(type))
            .collect(Collectors.toMap(IPrivateStepProcessor::getId, Function.identity()));
    }

    @Override
    public SdkPrivateWizard get(SdkPrivateWizard wizard) {
        String currentWizardId = UUID.randomUUID().toString();
        return SdkPrivateWizard.builder()
            .withCurrent(1)
            .withId(currentWizardId)
            .withStep(buildFirstStepData(currentWizardId, wizard))
            .build();
    }

    @Override
    public SdkPrivateWizard next(SdkPrivateWizard wizard) {
        PrivateWizardUtils.clearErrors(wizard, wizard.getCurrent());
        wizard.getStep().stream()
            .filter(s -> s.getId() > wizard.getCurrent())
            .map(SdkPrivateStep::getData)
            .forEach(SdkPrivateStepData::reset);
        validateCurrentStep(wizard);
        if (!wizard.isValid()) {
            return wizard;
        }
        processNextStep(wizard);
        return wizard;
    }

    @Override
    public SdkPrivateWizard prev(SdkPrivateWizard wizard) {
        wizard.setCurrent(wizard.getCurrent() - 1);
        List<SdkPrivateStep> steps = wizard.getStep();
        steps.stream()
            .filter(s -> s.getId() > wizard.getCurrent())
            .map(SdkPrivateStep::getData)
            .forEach(SdkPrivateStepData::reset);
        return wizard;
    }

    protected List<SdkPrivateStep> buildFirstStepData(String currentWizardId, SdkPrivateWizard previousWizard) {
        return stepProcessors.values().stream()
            .sorted(Comparator.comparingInt(IPrivateStepProcessor::getId))
            .map(p -> p.buildFirstStepData(currentWizardId, previousWizard))
            .collect(Collectors.toList());
    }

    protected void processNextStep(SdkPrivateWizard wizard) {
        Integer current = wizard.getCurrent();
        IPrivateStepProcessor processor = stepProcessors.get(current);
        if (processor == null) {
            throw new IllegalArgumentException("Incorrect step number received");
        }
        processor.prepareNextStep(wizard);
        if (wizard.isValid()) {
            wizard.setCurrent(current + 1);
        }
    }

    protected void validateCurrentStep(SdkPrivateWizard wizard) {
        Integer current = wizard.getCurrent();
        IPrivateStepProcessor processor = stepProcessors.get(current);
        if (processor == null) {
            throw new IllegalArgumentException("Incorrect step number received");
        }
        processor.validate(wizard);
    }

    @Override
    public String type() {
        return type;
    }
}
