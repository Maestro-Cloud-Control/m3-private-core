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
import java.util.List;

public class AmqpRegionStatistic {
    private static final String UNKNOWN = "unknown";

    private String host = UNKNOWN;
    private String username = UNKNOWN;
    private String port = UNKNOWN;
    private String vhost = UNKNOWN;
    private long latency = 9999;
    private boolean outdated;
    private long channels;
    private long connections;
    private long allConsumedMessages;
    private double consumedMessagesPerFiveMin;
    private long allPublishedMessages;
    private double publishedMessagesPerFiveMin;
    private List<MessageStatistics> queueStats = Collections.emptyList();

    public List<MessageStatistics> getQueueStats() {
        return queueStats;
    }

    public void setQueueStats(List<MessageStatistics> queueStats) {
        this.queueStats = queueStats;
    }

    public long getAllPublishedMessages() {
        return allPublishedMessages;
    }

    public void setAllPublishedMessages(long allPublishedMessages) {
        this.allPublishedMessages = allPublishedMessages;
    }

    public double getPublishedMessagesPerFiveMin() {
        return publishedMessagesPerFiveMin;
    }

    public void setPublishedMessagesPerFiveMin(double publishedMessagesPerFiveMin) {
        this.publishedMessagesPerFiveMin = publishedMessagesPerFiveMin;
    }

    public long getChannels() {
        return channels;
    }

    public void setChannels(long channels) {
        this.channels = channels;
    }

    public long getConnections() {
        return connections;
    }

    public void setConnections(long connections) {
        this.connections = connections;
    }

    public long getAllConsumedMessages() {
        return allConsumedMessages;
    }

    public void setAllConsumedMessages(long allConsumedMessages) {
        this.allConsumedMessages = allConsumedMessages;
    }

    public double getConsumedMessagesPerFiveMin() {
        return consumedMessagesPerFiveMin;
    }

    public void setConsumedMessagesPerFiveMin(double consumedMessagesPerFiveMin) {
        this.consumedMessagesPerFiveMin = consumedMessagesPerFiveMin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public boolean isOutdated() {
        return outdated;
    }

    public void setOutdated(boolean outdated) {
        this.outdated = outdated;
    }
}
