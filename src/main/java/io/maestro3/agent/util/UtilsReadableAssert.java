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

import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.Collection;
import java.util.Map;

public class UtilsReadableAssert {

    public static void assertEquals(int expected, int actual, String message) throws ReadableAgentException {
        assertEquals(expected, actual, message, null);
    }

    private static void assertEquals(int expected, int actual, String message, Exception e) throws ReadableAgentException {
        if (expected != actual) {
            if (e == null) {
                throw new ReadableAgentException(message);
            } else {
                throw new ReadableAgentException(message, e);
            }
        }
    }

    public static void assertEquals(long expected, long actual, String message) throws ReadableAgentException {
        equals(expected, actual, message, null);
    }

    private static void equals(long expected, long actual, String message, Exception e) throws ReadableAgentException {
        if (expected != actual) {
            if (e == null) {
                throw new ReadableAgentException(message);
            } else {
                throw new ReadableAgentException(message, e);
            }
        }
    }

    public static void notNull(Object o, String message) throws ReadableAgentException {
        notNull(o, message, null);
    }

    public static void notNull(Object o, String message, Exception e) throws ReadableAgentException {
        if (o == null) {
            if (e == null) {
                throw new ReadableAgentException(message);
            } else {
                throw new ReadableAgentException(message, e);
            }
        }
    }

    public static void isNull(Object o, String message) throws ReadableAgentException {
        isNull(o, message, null);
    }

    public static void isNull(Object o, String message, Exception e) throws ReadableAgentException {
        if (o != null) {
            if (e == null) {
                throw new ReadableAgentException(message);
            } else {
                throw new ReadableAgentException(message, e);
            }
        }
    }

    public static void notEmpty(Collection o, String message) throws ReadableAgentException {
        notEmpty(o, message, null);
    }

    public static void isEmpty(Collection<?> o, String message) {
        if (CollectionUtils.isNotEmpty(o)) {
            throw new ReadableAgentException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map, String message) {
        if (MapUtils.isEmpty(map)) {
            throw new ReadableAgentException(message);
        }
    }

    public static void notEmpty(Collection o, String message, Exception e) throws ReadableAgentException {
        if (CollectionUtils.isEmpty(o)) {
            if (e == null) {
                throw new ReadableAgentException(message);
            } else {
                throw new ReadableAgentException(message, e);
            }
        }
    }

    public static void hasLength(String string, String message) throws ReadableAgentException {
        hasLength(string, message, null);
    }

    public static void hasLength(String string, String message, Exception e) throws ReadableAgentException {
        if (StringUtils.isBlank(string)) {
            if (e == null) {
                throw new ReadableAgentException(message);
            } else {
                throw new ReadableAgentException(message, e);
            }
        }
    }

    public static void isTrue(boolean expression, String message) throws ReadableAgentException {
        isTrue(expression, message, null);
    }

    public static void isTrue(boolean expression, String message, Exception e) throws ReadableAgentException {
        if (!expression) {
            if (e == null) {
                throw new ReadableAgentException(message);
            } else {
                throw new ReadableAgentException(message, e);
            }
        }
    }

    public static void isFalse(boolean expression, String message) throws ReadableAgentException {
        isFalse(expression, message, null);
    }

    public static void isFalse(boolean expression, String message, Exception e) throws ReadableAgentException {
        if (expression) {
            if (e == null) {
                throw new ReadableAgentException(message);
            } else {
                throw new ReadableAgentException(message, e);
            }
        }
    }

    public static void fail(String message) throws ReadableAgentException {
        throw new ReadableAgentException(message);
    }
}
