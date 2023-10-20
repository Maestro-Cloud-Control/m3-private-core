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

package io.maestro3.agent.amqp.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExtendedHttpStatistic {
    private int statisticTime;
    private int totalRequests;
    private double errorRate;
    private Map<String, RequestInfo> totalRequestsByUrl = Collections.emptyMap();
    private double maxRequestTime;
    private double minRequestTime;

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public double getMaxRequestTime() {
        return maxRequestTime;
    }

    public void setMaxRequestTime(double maxRequestTime) {
        this.maxRequestTime = maxRequestTime;
    }

    public double getMinRequestTime() {
        return minRequestTime;
    }

    public void setMinRequestTime(double minRequestTime) {
        this.minRequestTime = minRequestTime;
    }

    public int getStatisticTime() {
        return statisticTime;
    }

    public void setStatisticTime(int statisticTime) {
        this.statisticTime = statisticTime;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public Map<String, RequestInfo> getTotalRequestsByUrl() {
        return totalRequestsByUrl;
    }

    public void setTotalRequestsByUrl(Map<String, RequestInfo> totalRequestsByUrl) {
        this.totalRequestsByUrl = totalRequestsByUrl;
    }


    public static class RequestInfo {
        private String url;
        private int requestsCount;
        private double errorRate;

        public RequestInfo(String url, int requestsCount, double errorRate) {
            this.url = url;
            this.requestsCount = requestsCount;
            this.errorRate = errorRate;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getRequestsCount() {
            return requestsCount;
        }

        public void setRequestsCount(int requestsCount) {
            this.requestsCount = requestsCount;
        }

        public double getErrorRate() {
            return errorRate;
        }

        public void setErrorRate(double errorRate) {
            this.errorRate = errorRate;
        }


        @Override
        public String toString() {
            return "RequestInfo{" +
                "url='" + url + '\'' +
                ", requestsCount=" + requestsCount +
                ", errorRate=" + errorRate +
                '}';
        }
    }

    @Override
    public String toString() {
        return "StatisticTime=" + statisticTime +
            ", errorRate=" + errorRate +
            ", maxRequestTime=" + maxRequestTime +
            ", minRequestTime=" + minRequestTime + System.lineSeparator() +
            totalRequestsByUrl.values()
                .stream()
                .map(Objects::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
