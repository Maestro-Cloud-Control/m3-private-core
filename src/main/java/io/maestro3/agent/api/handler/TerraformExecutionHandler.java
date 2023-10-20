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

package io.maestro3.agent.api.handler;

import io.maestro3.agent.terraform.console.engine.IConsoleCommandEngine;
import io.maestro3.agent.terraform.console.model.CommandExecutionResult;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.terraform.SdkPrivateAgentConsoleExecutionResponse;
import io.maestro3.sdk.v3.request.agent.SdkPrivateAgentConsoleExecutionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class TerraformExecutionHandler extends AbstractM3ApiHandler<SdkPrivateAgentConsoleExecutionRequest, SdkPrivateAgentConsoleExecutionResponse> {

    private final IConsoleCommandEngine consoleCommandEngine;

    @Value("${flag.enable.tf.filepath.replacer:false}")
    private boolean enabledTfFilepathReplacer;
    @Value("${tf.filepath.replace.from:/tf}")
    private String replaceFrom;
    @Value("${tf.filepath.replace.to:/pa}")
    private String replaceTo;

    @Autowired
    public TerraformExecutionHandler(IConsoleCommandEngine consoleCommandEngine) {
        super(SdkPrivateAgentConsoleExecutionRequest.class, ActionType.CONSOLE_EXECUTION);
        this.consoleCommandEngine = consoleCommandEngine;
    }

    @Override
    protected SdkPrivateAgentConsoleExecutionResponse handlePayload(ActionType action, SdkPrivateAgentConsoleExecutionRequest request) throws Exception {
        try {
            Set<String> deletedFiles = deleteFiles(request.getWorkingDirectoryFullPathName(), request.getToBeDeletedFullFileNames());
            Set<String> copiedFiles = copyFiles(request.getFullFileNamesWithContent());

            SdkPrivateAgentConsoleExecutionResponse response = new SdkPrivateAgentConsoleExecutionResponse()
                .withSyncedFullFilenames(copiedFiles)
                .withDeletedFullFileNames(deletedFiles);
            if (CollectionUtils.isNotEmpty(request.getCommandTokens())) {
                CommandExecutionResult result = consoleCommandEngine.runCommand(
                    request.getCommandTokens(), request.getEnvVariables(), replace(request.getWorkingDirectoryFullPathName()));
                response.withPositiveResult(result.getPositiveResult())
                    .withNegativeResult(result.getErrorResult())
                    .withExitCode(result.getExitCode());
            }
            Map<String, String> requestedFiles = prepareRequestedFiles(request.getRequestedFullFileNames());
            boolean dirExists = isDirExists(replace(request.getDirExists()));
            response.withFullFileNamesWithContent(requestedFiles);
            response.withDirExists(dirExists);
            return response;
        } catch (Exception ex) {
            LOG.error("Cannot execute command: {}", ex.getMessage(), ex);
            return new SdkPrivateAgentConsoleExecutionResponse()
                .withExitCode(1)
                .withNegativeResult(ex.getMessage());
        }
    }

    private boolean isDirExists(String dirPath) {
        if (dirPath == null || dirPath.isEmpty()) {
            return false;
        }
        File dir = new File(dirPath);
        return dir.exists() && dir.isDirectory();
    }

    private String replace(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        return enabledTfFilepathReplacer
            ? path.replaceAll(replaceFrom, replaceTo)
            : path;
    }

    private String replaceBack(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        return enabledTfFilepathReplacer
            ? path.replaceAll(replaceTo, replaceFrom)
            : path;
    }

    private Set<String> deleteFiles(String workingBaseDirectory, Set<String> toBeDeletedFullFileNames) {
        if (toBeDeletedFullFileNames == null || toBeDeletedFullFileNames.isEmpty()) {
            return new HashSet<>();
        }
        File workingBaseDirectoryFile = new File(replace(workingBaseDirectory));
        Set<String> deletedFiles = new HashSet<>();
        for (String toBeDeletedFullFileName : toBeDeletedFullFileNames) {
            File toBeDeleted = new File(replace(toBeDeletedFullFileName));
            if (toBeDeleted.exists()) {
                if (!toBeDeleted.getAbsolutePath().startsWith(workingBaseDirectoryFile.getAbsolutePath())) {
                    throw new IllegalArgumentException("Cannot delete file: " + toBeDeleted.getAbsolutePath() + ". It does not belong to dir: " + workingBaseDirectoryFile.getAbsolutePath());
                }
                boolean deleted = deleteRecursively(toBeDeleted);
                if (deleted) {
                    deletedFiles.add(toBeDeletedFullFileName);
                }
            }
        }
        return deletedFiles;
    }

    private Map<String, String> prepareRequestedFiles(Set<String> requestedFullFileNames) throws IOException {
        if (requestedFullFileNames == null || requestedFullFileNames.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, String> files = new HashMap<>();
        for (String requestedFullFileName : requestedFullFileNames) {
            try {
                File file = new File(replace(requestedFullFileName));
                if (file.exists()) {
                    String content = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(replace(requestedFullFileName))));
                    files.put(requestedFullFileName, content);
                }
            } catch (IOException ex) {
                LOG.error("Cannot read file {}", requestedFullFileName, ex);
                throw ex;
            }
        }
        return files;
    }

    private Set<String> copyFiles(Map<String, String> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> copiedFiles = new HashSet<>();
        for (Map.Entry<String, String> e : files.entrySet()) {
            String fullPath = replace(e.getKey());
            String encodedContent = e.getValue();
            try {
                byte[] decoded = Base64.getDecoder().decode(encodedContent);
                File checkParentDirs = new File(fullPath).getParentFile();
                boolean created = checkParentDirs.mkdirs();
                if (created) {
                    LOG.info("Created parent directories for: {}", checkParentDirs.getAbsolutePath());
                }
                Files.write(Paths.get(fullPath), decoded);
                copiedFiles.add(replaceBack(fullPath));
            } catch (IOException ex) {
                LOG.error("Cannot write file {}", e.getKey(), ex);
                throw ex;
            }
        }
        return copiedFiles;
    }

    boolean deleteRecursively(File toBeDeleted) {
        File[] allContents = toBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteRecursively(file);
            }
        }
        return toBeDeleted.delete();
    }

    @Override
    public SdkCloud getSupportedCloud() {
        // all clouds
        return null;
    }
}
