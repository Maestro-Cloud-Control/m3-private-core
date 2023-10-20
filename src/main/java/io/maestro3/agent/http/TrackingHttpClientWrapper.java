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

package io.maestro3.agent.http;

import io.maestro3.agent.http.tracker.IHttpRequestTracker;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;

import java.io.IOException;


public class TrackingHttpClientWrapper implements HttpClient {

    private static final String ERROR_STATUS = "ERROR";
    private final HttpClient baseClient;
    private final IHttpRequestTracker requestTracker;
    private final String clientRegionId;

    public TrackingHttpClientWrapper(HttpClient baseClient, IHttpRequestTracker requestTracker, String clientRegionId) {
        this.baseClient = baseClient;
        this.requestTracker = requestTracker;
        this.clientRegionId = clientRegionId;
    }

    @Override
    public HttpParams getParams() {
        return baseClient.getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return baseClient.getConnectionManager();
    }

    @Override
    public HttpResponse execute(HttpUriRequest httpUriRequest) throws IOException, ClientProtocolException {
        HttpResponse execute = null;
        long requestTime = 0L;
        String executeStatus = ERROR_STATUS;
        try {
            long startTime = System.currentTimeMillis();
            execute = baseClient.execute(httpUriRequest);
            requestTime = System.currentTimeMillis() - startTime;
            executeStatus = String.valueOf(execute.getStatusLine().getStatusCode());
        } finally {
            requestTracker.trackRequest(clientRegionId,
                httpUriRequest.getURI().toString(),
                executeStatus,
                HttpMethod.resolve(httpUriRequest.getMethod()),
                requestTime);
        }
        return execute;
    }

    @Override
    public HttpResponse execute(HttpUriRequest httpUriRequest, HttpContext httpContext) throws IOException, ClientProtocolException {
        HttpResponse execute = null;
        long requestTime = 0L;
        String executeStatus = ERROR_STATUS;
        try {
            long startTime = System.currentTimeMillis();
            execute = baseClient.execute(httpUriRequest, httpContext);
            requestTime = System.currentTimeMillis() - startTime;
            executeStatus = String.valueOf(execute.getStatusLine().getStatusCode());
        } finally {
            requestTracker.trackRequest(clientRegionId,
                httpUriRequest.getURI().toString(),
                executeStatus,
                HttpMethod.resolve(httpUriRequest.getMethod()),
                requestTime);
        }
        return execute;
    }

    @Override
    public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest) throws IOException, ClientProtocolException {
        HttpResponse execute = null;
        long requestTime = 0L;
        String executeStatus = ERROR_STATUS;
        try {
            long startTime = System.currentTimeMillis();
            execute = baseClient.execute(httpHost, httpRequest);
            requestTime = System.currentTimeMillis() - startTime;
            executeStatus = String.valueOf(execute.getStatusLine().getStatusCode());
        } finally {
            requestTracker.trackRequest(clientRegionId,
                httpHost.toString() + httpRequest.getRequestLine().getUri(),
                executeStatus,
                HttpMethod.resolve(httpRequest.getRequestLine().getMethod()),
                requestTime);
        }
        return execute;
    }

    @Override
    public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) throws IOException, ClientProtocolException {
        HttpResponse execute = null;
        long requestTime = 0L;
        String executeStatus = ERROR_STATUS;
        try {
            long startTime = System.currentTimeMillis();
            execute = baseClient.execute(httpHost, httpRequest, httpContext);
            requestTime = System.currentTimeMillis() - startTime;
            executeStatus = String.valueOf(execute.getStatusLine().getStatusCode());
        } finally {
            requestTracker.trackRequest(clientRegionId,
                httpHost.toString() + httpRequest.getRequestLine().getUri(),
                executeStatus,
                HttpMethod.resolve(httpRequest.getRequestLine().getMethod()),
                requestTime);
        }
        return execute;
    }

    @Override
    public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        boolean success = true;
        ResponseCaptor<? extends T> captor = new ResponseCaptor<>(responseHandler);
        T execute;
        long requestTime = 0L;
        try {
            long startTime = System.currentTimeMillis();
            execute = baseClient.execute(httpUriRequest, captor);
            requestTime = System.currentTimeMillis() - startTime;
        } catch (Throwable ex) {
            success = false;
            throw ex;
        } finally {
            requestTracker.trackRequest(clientRegionId,
                httpUriRequest.getURI().toString(),
                success ? String.valueOf(captor.getResponse().getStatusLine().getStatusCode()) : ERROR_STATUS,
                HttpMethod.resolve(httpUriRequest.getMethod()),
                requestTime);
        }
        return execute;
    }

    @Override
    public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
        boolean success = true;
        ResponseCaptor<? extends T> captor = new ResponseCaptor<>(responseHandler);
        T execute;
        long requestTime = 0L;
        try {
            long startTime = System.currentTimeMillis();
            execute = baseClient.execute(httpUriRequest, captor, httpContext);
            requestTime = System.currentTimeMillis() - startTime;
        } catch (Throwable ex) {
            success = false;
            throw ex;
        } finally {
            requestTracker.trackRequest(clientRegionId,
                httpUriRequest.getURI().toString(),
                success ? String.valueOf(captor.getResponse().getStatusLine().getStatusCode()) : ERROR_STATUS,
                HttpMethod.resolve(httpUriRequest.getMethod()),
                requestTime);
        }
        return execute;
    }

    @Override
    public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        boolean success = true;
        ResponseCaptor<? extends T> captor = new ResponseCaptor<>(responseHandler);
        T execute;
        long requestTime = 0L;
        try {
            long startTime = System.currentTimeMillis();
            execute = baseClient.execute(httpHost, httpRequest, captor);
            requestTime = System.currentTimeMillis() - startTime;
        } catch (Throwable ex) {
            success = false;
            throw ex;
        } finally {
            requestTracker.trackRequest(clientRegionId,
                httpHost.toString() + httpRequest.getRequestLine().getUri(),
                success ? String.valueOf(captor.getResponse().getStatusLine().getStatusCode()) : ERROR_STATUS,
                HttpMethod.resolve(httpRequest.getRequestLine().getMethod()),
                requestTime);
        }
        return execute;
    }

    @Override
    public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
        boolean success = true;
        ResponseCaptor<? extends T> captor = new ResponseCaptor<>(responseHandler);
        T execute;
        long requestTime = 0L;
        try {
            long startTime = System.currentTimeMillis();
            execute = baseClient.execute(httpHost, httpRequest, captor, httpContext);
            requestTime = System.currentTimeMillis() - startTime;
        } catch (Throwable ex) {
            success = false;
            throw ex;
        } finally {
            requestTracker.trackRequest(clientRegionId,
                httpHost.toString() + httpRequest.getRequestLine().getUri(),
                success ? String.valueOf(captor.getResponse().getStatusLine().getStatusCode()) : ERROR_STATUS,
                HttpMethod.resolve(httpRequest.getRequestLine().getMethod()),
                requestTime);
        }
        return execute;
    }


    private class ResponseCaptor<T> implements ResponseHandler<T> {
        private ResponseHandler<T> baseHandler;
        private HttpResponse httpResponse;

        public ResponseCaptor(ResponseHandler<T> baseHandler) {
            this.baseHandler = baseHandler;
        }

        @Override
        public T handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
            this.httpResponse = httpResponse;
            return baseHandler.handleResponse(httpResponse);
        }

        public HttpResponse getResponse() {
            return httpResponse;
        }
    }
}
