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

package io.maestro3.agent.util;

import io.maestro3.sdk.internal.util.StringUtils;


public enum UnitUtils {

    YOTTA("Y", Math.pow(2, 80)),
    ZETTA("Z", Math.pow(2, 70)),
    EXA("E", Math.pow(2, 60)),
    PETA("P", Math.pow(2, 50)),
    TERA("T", Math.pow(2, 40)),
    GIGA("G", Math.pow(2, 30)),
    MEGA("M", Math.pow(2, 20)),
    KILO("k", Math.pow(2, 10)),
    NONE(StringUtils.EMPTY_STRING, 1);

    private double multiplier;
    private String prefix;

    UnitUtils(String prefix, double multiplier) {
        this.prefix = prefix;
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public String getPrefix() {
        return prefix;
    }

    public long convertToLong(long value, UnitUtils unit) {
        return Math.round(convert(value, unit));
    }
    public double convert(double value, UnitUtils unit) {
        return value * this.multiplier / unit.multiplier;
    }
}
