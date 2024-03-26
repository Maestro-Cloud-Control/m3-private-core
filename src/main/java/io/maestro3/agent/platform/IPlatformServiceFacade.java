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

package io.maestro3.agent.platform;

import io.maestro3.agent.platform.model.PlatformService;
import io.maestro3.agent.platform.model.PlatformServiceEntry;
import io.maestro3.agent.tf.integration.model.TemplateStatus;

/**
 * @author Serhii Akhmetshin
 * Created: 23/02/2024
 */
public interface IPlatformServiceFacade {
    PlatformService findServiceByName(String name);

    PlatformServiceEntry findServiceEntryById(String id);

    void saveEntry(PlatformService service);

    void saveEntry(PlatformServiceEntry service);

    void deleteService(String name);

    String getRelatedStack(String entryId);

    void deleteEntry(String id);

    void entryStateChanged(String paasId, TemplateStatus newStatus);
}
