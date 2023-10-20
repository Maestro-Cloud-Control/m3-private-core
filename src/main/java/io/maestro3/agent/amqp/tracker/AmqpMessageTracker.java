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

package io.maestro3.agent.amqp.tracker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.maestro3.agent.amqp.model.AmqpRegionStatistic;
import io.maestro3.agent.amqp.model.IRabbitConfiguration;
import io.maestro3.agent.amqp.model.MessageStatistics;
import io.maestro3.agent.amqp.model.MessageStatisticsType;
import com.rabbitmq.client.MetricsCollector;
import com.rabbitmq.client.impl.StandardMetricsCollector;
import org.springframework.data.util.Pair;
import org.springframework.integration.amqp.support.AmqpHeaderMapper;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AmqpMessageTracker implements IAmqpMessageTracker {
    private static final int CACHE_TIME = 60; // in sec
    private static final Cache<String, AmqpRegionStatistic> CACHE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(CACHE_TIME, TimeUnit.SECONDS)
            .build();

    private final Map<String, Map<String, MessageStatistics>> readStatistics = new HashMap<>();
    private final Map<String, Map<String, MessageStatistics>> writeStatistics = new HashMap<>();
    private final Map<String, Pair<IRabbitConfiguration, StandardMetricsCollector>> collectorsMap = new HashMap<>();

    @Override
    public AmqpRegionStatistic collectStatistic(String regionId) {
        AmqpRegionStatistic statistic = CACHE.getIfPresent(regionId);
        if (statistic == null) {
            statistic = collectStatisticForRegion(regionId);
            CACHE.put(regionId, statistic);
        }
        return statistic;
    }

    private AmqpRegionStatistic collectStatisticForRegion(String regionId) {
        AmqpRegionStatistic statistic = collectBaseInfo(regionId);
        List<MessageStatistics> queuesStatistic = new ArrayList<>();
        if (readStatistics.containsKey(regionId)) {
            Map<String, MessageStatistics> readByRegion = readStatistics.get(regionId);
            queuesStatistic.addAll(readByRegion.values());
        }
        if (writeStatistics.containsKey(regionId)) {
            Map<String, MessageStatistics> writeByRegion = writeStatistics.get(regionId);
            queuesStatistic.addAll(writeByRegion.values());
        }
        statistic.setQueueStats(queuesStatistic);
        return statistic;
    }

    private AmqpRegionStatistic collectBaseInfo(String regionId) {
        AmqpRegionStatistic statistic = new AmqpRegionStatistic();
        Pair<IRabbitConfiguration, StandardMetricsCollector> pair = collectorsMap.get(regionId);
        if (pair == null) {
            statistic.setOutdated(true);
            return statistic;
        }
        IRabbitConfiguration configuration = pair.getFirst();
        StandardMetricsCollector collector = pair.getSecond();
        statistic.setHost(configuration.getRabbitHost());
        statistic.setPort(String.valueOf(configuration.getRabbitPort()));
        statistic.setVhost(configuration.getRabbitVirtHost());
        statistic.setUsername(configuration.getRabbitUsername());
        statistic.setChannels(collector.getChannels().getCount());
        statistic.setConnections(collector.getConnections().getCount());
        statistic.setAllConsumedMessages(collector.getConsumedMessages().getCount());
        statistic.setConsumedMessagesPerFiveMin(collector.getConsumedMessages().getFiveMinuteRate());
        statistic.setAllPublishedMessages(collector.getPublishedMessages().getCount());
        statistic.setPublishedMessagesPerFiveMin(collector.getPublishedMessages().getFiveMinuteRate());
        long hostLatency = getHostLatency(configuration.getRabbitHost(), configuration.getRabbitPort());
        statistic.setLatency(hostLatency);
        if (hostLatency>=2000){
            statistic.setOutdated(true);
        }
        return statistic;
    }

    @Override
    public MetricsCollector registerMetricCollector(String regionId, IRabbitConfiguration configuration) {
        StandardMetricsCollector collector = new StandardMetricsCollector();
        collectorsMap.put(regionId, Pair.of(configuration, collector));
        return collector;
    }

    @Override
    public void registerMetricCollector(String regionId, IRabbitConfiguration configuration, MetricsCollector collector) {
        collectorsMap.put(regionId, Pair.of(configuration, (StandardMetricsCollector) collector));
    }

    @Override
    public void trackSend(String regionId, String queue) {
        synchronized (writeStatistics) {
            Map<String, MessageStatistics> statisticsMap = this.writeStatistics.get(regionId);
            if (statisticsMap == null) {
                statisticsMap = new HashMap<>();
                writeStatistics.put(regionId, statisticsMap);
            }
            MessageStatistics statistics = statisticsMap.get(queue);
            if (statistics == null) {
                statisticsMap.put(queue, statistics = new MessageStatistics(queue, MessageStatisticsType.WRITE));
            }
            statistics.setCount(statistics.getCount() + 1);
            statistics.setLastUsage(System.currentTimeMillis());
        }
    }

    @Override
    public void trackReceive(String regionId, String queue) {
        synchronized (readStatistics) {
            Map<String, MessageStatistics> statisticsMap = this.readStatistics.get(regionId);
            if (statisticsMap == null) {
                statisticsMap = new HashMap<>();
                readStatistics.put(regionId, statisticsMap);
            }
            MessageStatistics statistics = statisticsMap.get(queue);
            if (statistics == null) {
                statisticsMap.put(queue, statistics = new MessageStatistics(queue, MessageStatisticsType.READ));
            }
            statistics.setCount(statistics.getCount() + 1);
            statistics.setLastUsage(System.currentTimeMillis());
        }
    }

    private long getHostLatency(String host, int port) {
        SocketAddress a = new InetSocketAddress(host, port);
        int timeoutMillis = 2000;
        long start = System.currentTimeMillis();
        long diff;
        try (Socket s = new Socket()) {
            s.connect(a, timeoutMillis);
            diff = System.currentTimeMillis() - start;
        } catch (Exception e) {
            diff = 9999;
        }
        return diff;
    }
}
