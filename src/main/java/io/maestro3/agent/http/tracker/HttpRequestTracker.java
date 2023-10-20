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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.maestro3.agent.amqp.model.ExtendedHttpStatistic;
import io.maestro3.agent.amqp.model.HttpRegionStatistic;
import io.maestro3.agent.http.collector.IExtendedHttpStatisticCollector;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static org.joda.time.DateTimeZone.UTC;


@Component
public class HttpRequestTracker implements IHttpRequestTracker {

    private static final Map<String, Pair<Cache<Long, HttpTrack>, Long>> METRICS = new HashMap<>();
    private static final long INIT_TIME = System.currentTimeMillis();
    private static final int DIFF_MIN = 5;
    private final int extendedRequestLoggingMin;
    private final IExtendedHttpStatisticCollector collector;

    public HttpRequestTracker(@Value("${extended.request.logging.minutes}") int extendedRequestLoggingMin,
                              IExtendedHttpStatisticCollector collector) {
        this.extendedRequestLoggingMin = extendedRequestLoggingMin;
        this.collector = collector;

    }

    @Override
    public synchronized HttpRegionStatistic collectStatistic(String regionId) {
        Pair<Cache<Long, HttpTrack>, Long> cacheLongPair = METRICS.get(regionId);
        HttpRegionStatistic httpRegionStatistic = new HttpRegionStatistic();
        if (cacheLongPair == null) {
            return httpRegionStatistic;
        }
        Long total = cacheLongPair.getSecond();
        double timeDiff = (System.currentTimeMillis() - INIT_TIME) / 1000.0 / 60.0;
        BigDecimal per5Min = BigDecimal.valueOf(total / timeDiff * DIFF_MIN);
        httpRegionStatistic.setRequestsPer5Min(per5Min.setScale(3, BigDecimal.ROUND_CEILING).doubleValue());
        httpRegionStatistic.setTotalRequests(total);
        if (extendedRequestLoggingMin > 0) {
            ConcurrentMap<Long, HttpTrack> httpRequests = cacheLongPair.getFirst().asMap();
            List<HttpTrack> tracks = new ArrayList<>(httpRequests.values());
            ExtendedHttpStatistic extendedHttpStatistic = collector.collect(tracks);
            extendedHttpStatistic.setStatisticTime(extendedRequestLoggingMin);
            httpRegionStatistic.setExtendedHttpStatistic(extendedHttpStatistic);
        }
        return httpRegionStatistic;
    }

    @Override
    public synchronized void trackRequest(String regionId, String url, String response, HttpMethod method, long requestTimeInMillis) {
        Pair<Cache<Long, HttpTrack>, Long> pair = METRICS.get(regionId);
        if (pair == null) {
            pair = Pair.of(CacheBuilder.newBuilder()
                .expireAfterWrite(extendedRequestLoggingMin, TimeUnit.MINUTES)
                .maximumSize(1000000)
                .build(), 0L);
        }
        Cache<Long, HttpTrack> requestsCache = pair.getFirst();
        Long totalRequests = pair.getSecond();
        Long currentTime = newDateTimeTimestamp();
        if (extendedRequestLoggingMin > 0) {
            requestsCache.put(currentTime, new HttpTrack(url, response, method, requestTimeInMillis));
        }

        METRICS.put(regionId, Pair.of(requestsCache, totalRequests + 1));
    }

    private Long newDateTimeTimestamp() {
        return DateTime.now(UTC).getMillis();
    }

}
