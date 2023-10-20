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

import com.fasterxml.jackson.core.type.TypeReference;
import io.maestro3.agent.util.ZipUtils;
import io.maestro3.sdk.exception.EncryptionException;
import io.maestro3.sdk.internal.M3SdkConstants;
import io.maestro3.sdk.internal.signer.IM3Signer;
import io.maestro3.sdk.internal.util.JsonUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.core.M3ApiAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.message.AuthException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class HeadersValidator {

    private static final Logger LOG = LoggerFactory.getLogger(HeadersValidator.class);

    private final IM3Signer signer;
    private final long requestExpiration;

    public HeadersValidator(IM3Signer signer, long requestExpiration) {
        this.signer = signer;
        this.requestExpiration = requestExpiration;
    }

    public void validate(Map<String, Object> headers) throws AuthException {
        long date = getDate(headers);
        if (System.currentTimeMillis() - date > requestExpiration) {
            LOG.error("Request is expired for date: {}, expiration date: {}", date, requestExpiration);
            throw new AuthException("Request is expired");
        }

        String signature = getSignature(headers);
        String accessId = getAccessId(headers);
        String userIdentifier = getUserIdentifier(headers);
        checkSign(signature, accessId, date, userIdentifier);
    }

    public List<M3ApiAction> validateAndDecrypt(Map<String, Object> headers, String encryptedM3ApiActionsJson) throws AuthException {
        validate(headers);
        String accessId = getAccessId(headers);
        String decryptedM3ApiActionsJson = signer.decrypt(encryptedM3ApiActionsJson, accessId);
        if (Boolean.TRUE.equals(headers.get(M3SdkConstants.COMPRESSED_HEADER))) {
            decryptedM3ApiActionsJson = ZipUtils.decompress(Base64.getDecoder().decode(decryptedM3ApiActionsJson));
        }
        return JsonUtils.parseJson(decryptedM3ApiActionsJson, new TypeReference<>() {
        });
    }

    private String getAccessId(Map<String, Object> headers) throws AuthException {
        String accessId = (String) headers.get(M3SdkConstants.ACCESS_KEY_HEADER);
        if (StringUtils.isBlank(accessId)) {
            LOG.error("Access id is unavailable");
            throw new AuthException("Access id is unavailable");
        }
        return accessId;

    }

    private String getUserIdentifier(Map<String, Object> headers) throws AuthException {
        String userIdentifier = (String) headers.get(M3SdkConstants.USER_IDENTIFIER_HEADER);
        if (StringUtils.isBlank(userIdentifier)) {
            String errorMessage = M3SdkConstants.USER_IDENTIFIER_HEADER + " is unavailable";
            throw new AuthException(errorMessage);
        }
        return userIdentifier;
    }

    private String getSignature(Map<String, Object> headers) throws AuthException {
        String signature = (String) headers.get(M3SdkConstants.AUTHENTICATION_HEADER);
        if (StringUtils.isBlank(signature)) {
            LOG.error("Authentication signature is unavailable");
            throw new AuthException("Authentication signature is unavailable");
        }
        return signature;
    }

    private long getDate(Map<String, Object> headers) throws AuthException {
        try {
            String headerValue = (String) headers.get(M3SdkConstants.DATE_HEADER);
            if (StringUtils.isBlank(headerValue)) {
                LOG.error("Request date is unavailable");
                throw new AuthException("Request date is unavailable");
            }
            return Long.parseLong(headerValue);
        } catch (NumberFormatException e) {
            throw new AuthException("Date is invalid");
        }
    }

    private void checkSign(String signature, String accessKey, long date, String userIdentifier) throws AuthException {
        String signatureIsInvalid = "Signature is invalid";
        String logErrorFormat = "Request signature is invalid for requester with accessKey: '{}'";
        try {
            boolean isValid = signer.isSignValid(signature, date, accessKey, userIdentifier);
            if (!isValid) {
                LOG.error(logErrorFormat, accessKey);
                throw new AuthException(signatureIsInvalid);
            }

        } catch (EncryptionException e) {
            LOG.error(logErrorFormat, accessKey, e);
            throw new AuthException(signatureIsInvalid);
        }
    }
}
