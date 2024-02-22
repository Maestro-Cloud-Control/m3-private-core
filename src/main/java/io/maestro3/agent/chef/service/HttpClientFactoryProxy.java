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

package io.maestro3.agent.chef.service;

import io.maestro3.agent.factory.CloseableHttpClientFactory;
import io.maestro3.agent.http.tracker.IHttpRequestTracker;
import io.maestro3.chef.client.http.client.ICloseableHttpClientFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Serhii Akhmetshin
 * Created: 19/12/2023
 */
@Service
public class HttpClientFactoryProxy implements ICloseableHttpClientFactory {
    private IHttpRequestTracker tracker;

    @Autowired
    public HttpClientFactoryProxy(IHttpRequestTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public HttpClient getHttpClient() {
        return CloseableHttpClientFactory.getHttpClient(0);
    }

    @Override
    public HttpClient getCountedHttpClient(String s) {
        return getHttpClient();
    }

    @Override
    public HttpClient getHttpClient(RequestConfig requestConfig) {
        return CloseableHttpClientFactory.getHttpClient(requestConfig,
                "chef",
                tracker);
    }

    @Override
    public HttpClient getHttpClient(boolean addFakeSocketFactory) {
        return CloseableHttpClientFactory.getHttpClient(addFakeSocketFactory);
    }
}
