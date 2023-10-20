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

import io.maestro3.agent.admin.AdminCommandPipeline;
import io.maestro3.agent.admin.IAdminCommandFactory;
import io.maestro3.sdk.v3.model.agent.wizard.SdkAdminCommand;
import io.maestro3.sdk.v3.model.agent.wizard.SdkAdminCommandPipeline;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;

import java.util.List;
import java.util.stream.Collectors;

public interface IPrivateWizardProcessor {

    SdkPrivateWizard get(SdkPrivateWizard wizard);

    SdkPrivateWizard next(SdkPrivateWizard wizard);

    SdkPrivateWizard prev(SdkPrivateWizard wizard);

    default SdkPrivateWizard verify(IAdminCommandFactory adminCommandFactory, SdkPrivateWizard wizard) {
        AdminCommandPipeline pipeline = AdminCommandPipeline.fromValue(type());
        List<SdkAdminCommand> commands = pipeline.getCommandTypes().stream()
            .map(adminCommandFactory::getCommand)
            .flatMap(c -> c.prepareCommands(c.getParams(wizard)).keySet().stream())
            .collect(Collectors.toList());
        wizard.setPipeline(new SdkAdminCommandPipeline().setCommands(commands));
        return wizard;
    }

    default SdkPrivateWizard execute(IAdminCommandFactory adminCommandFactory, SdkPrivateWizard wizard) {
        AdminCommandPipeline pipeline = AdminCommandPipeline.fromValue(type());
        List<SdkAdminCommand> commands = pipeline.getCommandTypes().stream()
            .map(adminCommandFactory::getCommand)
            .flatMap(c -> c.executeCommand(c.getParams(wizard)).stream())
            .collect(Collectors.toList());
        wizard.setPipeline(new SdkAdminCommandPipeline().setCommands(commands));
        return wizard;
    }

    String type();
}
