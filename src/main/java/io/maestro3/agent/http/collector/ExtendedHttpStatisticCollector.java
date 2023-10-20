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

package io.maestro3.agent.http.collector;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.maestro3.agent.amqp.model.ExtendedHttpStatistic;
import io.maestro3.agent.http.tracker.HttpTrack;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


@Component
public class ExtendedHttpStatisticCollector implements IExtendedHttpStatisticCollector {

    private static final Pattern UUID_PATTERN = Pattern.compile("([a-z0-9]{8}(-[a-z0-9]{4}){4}[a-z0-9]{8})");
    private static final Set<String> AVAILABLE_TRACK_STATES = new HashSet<>();

    static {
        AVAILABLE_TRACK_STATES.addAll(Arrays.asList(
            "200",
            "201",
            "202",
            "204",
            "205",
            "206",
            "207",
            "208",
            "100",
            "101",
            "102",
            "404"
        ));
    }

    @Override
    public ExtendedHttpStatistic collect(List<HttpTrack> httpTracks) {
        if (CollectionUtils.isEmpty(httpTracks)) {
            return null;
        }
        ExtendedHttpStatistic statistic = new ExtendedHttpStatistic();
        long minTime = Integer.MAX_VALUE;
        long maxTime = -1;
        int errorCount = 0;
        Multimap<String, HttpTrack> trackMultimap = ArrayListMultimap.create();
        String url;
        for (HttpTrack httpTrack : httpTracks) {
            url = getResolvedUrl(httpTrack);
            maxTime = httpTrack.getRequestTimeInMillis() >= maxTime ? httpTrack.getRequestTimeInMillis() : maxTime;
            minTime = httpTrack.getRequestTimeInMillis() < minTime ? httpTrack.getRequestTimeInMillis() : minTime;
            errorCount = isTrackSuccess(httpTrack) ? errorCount : errorCount + 1;
            trackMultimap.put(url, httpTrack);
        }
        statistic.setErrorRate(getRate(errorCount, httpTracks.size()));
        statistic.setMaxRequestTime(maxTime/1000.0);
        statistic.setTotalRequests(httpTracks.size());
        statistic.setMinRequestTime(minTime/1000.0);
        statistic = fillStatisticByUrl(statistic, trackMultimap);
        return statistic;
    }

    private String getResolvedUrl(HttpTrack httpTrack) {
        return UUID_PATTERN.matcher(httpTrack.getUrl().split("\\?")[0])
            .replaceAll("{id}");
    }

    private ExtendedHttpStatistic fillStatisticByUrl(ExtendedHttpStatistic statistic, Multimap<String, HttpTrack> trackMultimap) {
        Map<String, ExtendedHttpStatistic.RequestInfo> totalRequestsByUrl = new HashMap<>();
        for (String url : trackMultimap.keySet()) {
            Collection<HttpTrack> httpTracksByUrl = trackMultimap.get(url);
            int errorCount = 0;
            for (HttpTrack httpTrack : httpTracksByUrl) {
                errorCount = isTrackSuccess(httpTrack) ? errorCount : errorCount + 1;
            }
            totalRequestsByUrl.put(url,
                new ExtendedHttpStatistic.RequestInfo(url, httpTracksByUrl.size(), getRate(errorCount, httpTracksByUrl.size())));
        }
        statistic.setTotalRequestsByUrl(totalRequestsByUrl);
        return statistic;
    }

    private boolean isTrackSuccess(HttpTrack httpTrack) {
        return AVAILABLE_TRACK_STATES.contains(httpTrack.getResponseCode());
    }

    private double getRate(int count, long total) {
        return count == 0
            ? 0
            : BigDecimal.valueOf(((double) count) / total).setScale(3, RoundingMode.CEILING).doubleValue() * 100;
    }
}
