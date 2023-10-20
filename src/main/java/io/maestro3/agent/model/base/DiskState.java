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

package io.maestro3.agent.model.base;


public enum DiskState {

    AVAILABLE("available"),
    IN_USE("in-use");

    private final String stateString;

    DiskState(String stateString) {
        this.stateString = stateString;
    }

    public String getStateString() {
        return stateString;
    }

    public static DiskState fromStateString(String stateString) {
        DiskState foundVolumeState = null;
        for (DiskState volumeState : values()) {
            if (volumeState.stateString.equals(stateString)) {
                foundVolumeState = volumeState;
                break;
            }
        }
        return foundVolumeState;
    }
}
