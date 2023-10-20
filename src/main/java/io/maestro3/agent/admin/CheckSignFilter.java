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

package io.maestro3.agent.admin;

import io.maestro3.agent.api.HeadersValidator;
import io.maestro3.agent.exception.M3PrivateAgentException;

import javax.security.auth.message.AuthException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class CheckSignFilter implements Filter {

    private final HeadersValidator validator;

    public CheckSignFilter(HeadersValidator validator) {
        this.validator = validator;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) req;

        Map<String, Object> headers = Collections
                .list(httpRequest.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, httpRequest::getHeader));
        try {
            validator.validate(headers);
        } catch (AuthException e) {
            throw new M3PrivateAgentException("Auth error. Illegal signature");
        }
        filterChain.doFilter(req, res);
    }
}
