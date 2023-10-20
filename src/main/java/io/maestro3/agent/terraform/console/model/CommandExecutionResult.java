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

package io.maestro3.agent.terraform.console.model;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class CommandExecutionResult {
    public static final int SUCCESS_EXIT_CODE = 0;
    public static final int ERROR_EXIT_CODE = 1;
    private String positiveResult;
    private String errorResult;
    private int exitCode;

    public CommandExecutionResult(String positiveResult, String errorResult, int exitCode) {
        this.positiveResult = positiveResult;
        this.errorResult = errorResult;
        this.exitCode = exitCode;
    }

    public String getPositiveResult() {
        return positiveResult;
    }

    public void setPositiveResult(String positiveResult) {
        this.positiveResult = positiveResult;
    }

    public String getErrorResult() {
        return errorResult;
    }

    public void setErrorResult(String errorResult) {
        this.errorResult = errorResult;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public String toString() {
        return "CommandExecutionResult{" +
            "positiveResult='" + positiveResult + '\'' +
            ", errorResult='" + errorResult + '\'' +
            ", exitCode=" +  exitCode +
            '}';
    }

    @JsonIgnore
    public boolean isSuccess() {
        return exitCode == SUCCESS_EXIT_CODE;
    }

    @JsonIgnore
    public boolean isNotSuccess() {
        return !isSuccess();
    }
}
