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

package io.maestro3.agent.api;

import io.maestro3.sdk.internal.provider.IM3CredentialsProvider;
import io.maestro3.sdk.internal.signer.impl.M3Signer;

public class PrivateAgentSigner extends M3Signer {
    public PrivateAgentSigner(IM3CredentialsProvider credentialsProvider) {
        super(credentialsProvider);
    }

    @Override
    public boolean isSignValid(String sign, long date, String accessKey, String userIdentifier) {
        String strDate = decrypt(sign, accessKey);
        return String.valueOf(date).equalsIgnoreCase(strDate);
    }
}
