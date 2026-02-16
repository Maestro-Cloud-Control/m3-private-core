/*
 * Copyright 2025 Maestro Cloud Control LLC
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
 */
package io.maestro3.agent.network.security;

import org.springframework.stereotype.Component;

@Component
public class PlainSecretResolver implements SecretResolver {

    @Override
    public String resolve(String secretRef) {
        if (secretRef == null || secretRef.isBlank()) {
            throw new IllegalArgumentException("secretRef is blank");
        }
        if (secretRef.startsWith("env:")) {
            String key = secretRef.substring("env:".length());
            String v = System.getenv(key);
            if (v == null || v.isBlank()) {
                throw new IllegalStateException("Missing env secret: " + key);
            }
            return v;
        }
        throw new IllegalArgumentException("Unsupported secret ref: " + secretRef);
    }
}
