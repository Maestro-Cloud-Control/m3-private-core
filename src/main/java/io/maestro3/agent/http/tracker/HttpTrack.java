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

package io.maestro3.agent.http.tracker;

import org.springframework.http.HttpMethod;


public class HttpTrack {
    private String url;
    private String responseCode;
    private HttpMethod method;
    private long requestTimeInMillis;

    public HttpTrack(String url, String responseCode, HttpMethod method, long requestTimeInMillis) {
        this.url = url;
        this.responseCode = responseCode;
        this.method = method;
        this.requestTimeInMillis = requestTimeInMillis;
    }

    public long getRequestTimeInMillis() {
        return requestTimeInMillis;
    }

    public void setRequestTimeInMillis(long requestTimeInMillis) {
        this.requestTimeInMillis = requestTimeInMillis;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
}
