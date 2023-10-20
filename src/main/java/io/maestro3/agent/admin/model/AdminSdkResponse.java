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

package io.maestro3.agent.admin.model;

import java.util.Collection;

public class AdminSdkResponse<T> {
    private final T data;
    private final ResponseType responseType;
    private final long timestamp = System.currentTimeMillis();

    public AdminSdkResponse(T data, ResponseType responseType) {
        this.data = data;
        this.responseType = responseType;
    }

    public T getData() {
        return data;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static <O> AdminSdkResponse<O> of(O object) {
        if (object instanceof String) {
            return new AdminSdkResponse<>(object, ResponseType.MESSAGE);
        } else if (object instanceof Collection){
            return new AdminSdkResponse<>(object, ResponseType.LIST);
        } else{
            return new AdminSdkResponse<>(object, ResponseType.OBJECT);
        }
    }

}
