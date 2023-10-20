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

public class HttpRegionStatistic {

    private long totalRequests;
    private double requestsPer5Min;
    private ExtendedHttpStatistic extendedHttpStatistic;

    public ExtendedHttpStatistic getExtendedHttpStatistic() {
        return extendedHttpStatistic;
    }

    public void setExtendedHttpStatistic(ExtendedHttpStatistic extendedHttpStatistic) {
        this.extendedHttpStatistic = extendedHttpStatistic;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public double getRequestsPer5Min() {
        return requestsPer5Min;
    }

    public void setRequestsPer5Min(double requestsPer5Min) {
        this.requestsPer5Min = requestsPer5Min;
    }


    @Override
    public String toString() {
        return "TotalRequests=" + totalRequests +
            ", requestsPer5Min=" + requestsPer5Min + System.lineSeparator() +
            extendedHttpStatistic.toString();
    }
}
