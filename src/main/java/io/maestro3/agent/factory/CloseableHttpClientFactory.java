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

package io.maestro3.agent.factory;

import io.maestro3.agent.http.TrackingHttpClientWrapper;
import io.maestro3.agent.http.tracker.IHttpRequestTracker;
import io.maestro3.agent.ssl.FakeSSLConnectionSocketFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class CloseableHttpClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CloseableHttpClientFactory.class);

    private static final Integer DEFAULT_CONN_TIMEOUT = 30000;
    private static final Integer DEFAULT_SOCKET_TIMEOUT = 60000;
    private static final Integer DEFAULT_POOL_MAX_TOTAL = 20;
    private static final Integer DEFAULT_POOL_PER_ROUTE = 2;

    private CloseableHttpClientFactory() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }

    public static HttpClient getHttpClient(String regionId, int timeout, IHttpRequestTracker requestTracker) {
        return getHttpClient(
            timeout * 1000,
            timeout * 2000,
            DEFAULT_POOL_MAX_TOTAL,
            DEFAULT_POOL_PER_ROUTE,
            true,
            regionId,
            requestTracker);
    }
    public static HttpClient getHttpClient(String regionId, IHttpRequestTracker requestTracker) {
        return getHttpClient(
            DEFAULT_CONN_TIMEOUT,
            DEFAULT_SOCKET_TIMEOUT,
            DEFAULT_POOL_MAX_TOTAL,
            DEFAULT_POOL_PER_ROUTE,
            true,
            regionId,
            requestTracker);
    }

    public static HttpClient getHttpClient(int timeoutSec) {
        return getHttpClient(
            timeoutSec == 0 ? DEFAULT_CONN_TIMEOUT : timeoutSec * 1000,
            DEFAULT_SOCKET_TIMEOUT,
            DEFAULT_POOL_MAX_TOTAL,
            DEFAULT_POOL_PER_ROUTE,
            true);
    }

    public static HttpClient getHttpClient(RequestConfig config,
                                           int poolMaxTotal,
                                           int poolPerRoute,
                                           String regionId,
                                           IHttpRequestTracker requestTracker) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(getSocketFactoryRegistry());
        connectionManager.setMaxTotal(poolMaxTotal);
        connectionManager.setDefaultMaxPerRoute(poolPerRoute);
        return new TrackingHttpClientWrapper(HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(config)
            .build(), requestTracker, regionId);
    }

    public static HttpClient getHttpClient(int connectionTimeout,
                                           int socketTimeout,
                                           int poolMaxTotal,
                                           int poolPerRoute,
                                           boolean addFakeFactory,
                                           String regionId,
                                           IHttpRequestTracker requestTracker) {
        RequestConfig.Builder requestBuilder = RequestConfig.custom()
            .setConnectTimeout(connectionTimeout)
            .setSocketTimeout(socketTimeout);

        PoolingHttpClientConnectionManager connectionManager;
        if (addFakeFactory) {
            connectionManager = new PoolingHttpClientConnectionManager(getSocketFactoryRegistry());
        } else {
            connectionManager = new PoolingHttpClientConnectionManager();
        }
        connectionManager.setMaxTotal(poolMaxTotal);
        connectionManager.setDefaultMaxPerRoute(poolPerRoute);
        return new TrackingHttpClientWrapper(HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestBuilder.build())
            .build(),
            requestTracker, regionId);
    }

    public static HttpClient getHttpClient(int connectionTimeout,
                                           int socketTimeout,
                                           int poolMaxTotal,
                                           int poolPerRoute,
                                           boolean addFakeFactory) {
        RequestConfig.Builder requestBuilder = RequestConfig.custom()
            .setConnectTimeout(connectionTimeout)
            .setSocketTimeout(socketTimeout);

        PoolingHttpClientConnectionManager connectionManager;
        if (addFakeFactory) {
            connectionManager = new PoolingHttpClientConnectionManager(getSocketFactoryRegistry());
        } else {
            connectionManager = new PoolingHttpClientConnectionManager();
        }
        connectionManager.setMaxTotal(poolMaxTotal);
        connectionManager.setDefaultMaxPerRoute(poolPerRoute);
        return HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestBuilder.build())
            .build();
    }

    private static Registry<ConnectionSocketFactory> getSocketFactoryRegistry() {
        try {
            return RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("HTTP", PlainConnectionSocketFactory.getSocketFactory())
                .register("HTTPS", FakeSSLConnectionSocketFactory.getInstance())
                .build();
        } catch (Exception e) {
            LOG.error("Registry creation failed", e);
            throw new IllegalStateException("Registry creation failed", e);
        }
    }
}
