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

package io.maestro3.agent.terraform.console.engine;

import io.maestro3.agent.terraform.console.exception.ConsoleCommandExecutionException;
import io.maestro3.agent.terraform.console.model.CommandExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
public class ConsoleCommandEngine implements IConsoleCommandEngine {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleCommandEngine.class);

    @Override
    public CommandExecutionResult runCommand(List<String> commandTokens) {
        return executeCommand(commandTokens, null, null);
    }

    @Override
    public CommandExecutionResult runCommand(List<String> command, String workingDirectory) {
        return runCommand(command, null, workingDirectory);
    }

    @Override
    public CommandExecutionResult runCommand(List<String> command, Map<String, String> envVariables, String workingDirectory) {
        if (workingDirectory == null || workingDirectory.isEmpty()) {
            throw new ConsoleCommandExecutionException("WorkingDirectory cannot be null or empty");
        }
        LOG.info("Tokens : {}", command);

        File workingDir = new File(workingDirectory);
        LOG.info("Is workingDir exists: {}, in '{}'", workingDir.exists(), workingDir.getAbsolutePath());
        return executeCommand(command, envVariables, workingDir);
    }

    private CommandExecutionResult executeCommand(List<String> commandTokens, Map<String, String> envVariables, File workingDirectory) {
        if (commandTokens == null || commandTokens.isEmpty()) {
            throw new ConsoleCommandExecutionException("Command cannot be null or empty");
        }

        String[] tokens = commandTokens.toArray(new String[]{});
        String[] environmentVariables = convertToEnvArray(envVariables);
        try {
            Process process = Runtime.getRuntime().exec(tokens, environmentVariables, workingDirectory);
            return getOutput(process);
        } catch (IOException e) {
            LOG.error("Failure during command execution: '{}'", commandTokens, e);
            throw new ConsoleCommandExecutionException("Failure during command execution: '" + commandTokens + "' " + e);
        } catch (InterruptedException e) {
            LOG.error("Failure during waiting for process completion", e);
            Thread.currentThread().interrupt();
            throw new ConsoleCommandExecutionException("Failure during waiting for process completion", e);
        }
    }

    private CommandExecutionResult getOutput(Process process) throws IOException, InterruptedException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        StringBuilder inputSb = new StringBuilder();
        StringBuilder errorSb = new StringBuilder();

        while (process.isAlive()) {
            // read output and error to prevent buffer block
            readAllAvailableLines(inputReader, inputSb);
            readAllAvailableLines(errorReader, errorSb);

            process.waitFor(100, TimeUnit.MILLISECONDS);
        }

        process.waitFor(10, TimeUnit.SECONDS);

        // read the left output and error data after process is terminated or timed out
        readAllAvailableLines(inputReader, inputSb);
        readAllAvailableLines(errorReader, errorSb);

        String error = errorSb.toString();
        return new CommandExecutionResult(inputSb.toString(), error, process.exitValue());
    }

    private void readAllAvailableLines(BufferedReader reader, StringBuilder resultLines) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (resultLines.length() > 0) {
                resultLines.append(System.lineSeparator());
            }
            resultLines.append(line);
        }
    }

    private static String[] convertToEnvArray(Map<String, String> envVariables) {
        if (envVariables == null || envVariables.isEmpty()) {
            return null;
        }

        return envVariables.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toArray(String[]::new);
    }
}
