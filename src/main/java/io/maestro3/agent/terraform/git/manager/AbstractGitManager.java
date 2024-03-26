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

package io.maestro3.agent.terraform.git.manager;

import io.maestro3.agent.terraform.git.IGitManager;
import io.maestro3.agent.terraform.git.IGitSecretManager;
import io.maestro3.agent.terraform.git.IGitWebhookSecretManager;
import io.maestro3.agent.terraform.git.exception.GitException;
import io.maestro3.agent.util.FileUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.apache.http.util.Asserts;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

public abstract class AbstractGitManager implements IGitManager {

    protected final IGitSecretManager secretManager;
    protected final IGitWebhookSecretManager webhookSecretManager;
    protected final String providerName;

    protected AbstractGitManager(IGitSecretManager secretManager,
                                 IGitWebhookSecretManager webhookSecretManager,
                                 final String providerName) {
        this.secretManager = secretManager;
        this.webhookSecretManager = webhookSecretManager;
        this.providerName = providerName;
    }

    @Override
    public void clone(char[] token, String gitUrl, File destinationDirectory, String branch, String repoSubDirectory) {
        Asserts.notNull(token, "token");
        Asserts.notBlank(gitUrl, "gitUrl");
        Asserts.notNull(destinationDirectory, "destinationDirectory");
        Asserts.notBlank(branch, "branch");

        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token);
        CloneCommand cloneCommand = Git.cloneRepository()
                .setCredentialsProvider(credentialsProvider)
                .setURI(gitUrl)
                .setDirectory(destinationDirectory)
                .setNoCheckout(true)
                .setDepth(1)
                .setBranchesToClone(Collections.singleton("refs/heads/" + branch))
                .setBranch(branch);
        try (Git git = cloneCommand.call()) {
            String fullBranchName = "origin/" + branch;
            CheckoutCommand checkoutCommand = git.checkout()
                    .setName(fullBranchName)
                    .setStartPoint(fullBranchName);
            if (StringUtils.isNotBlank(repoSubDirectory)) {
                checkoutCommand.addPath(repoSubDirectory);
            } else {
                checkoutCommand.setAllPaths(true);
            }
            checkoutCommand.call();
        } catch (GitAPIException e) {
            String errorMessage = String.format(
                    "Failed to clone repository to %s directory. Repository url: %s, branch: %s, subPath: %s. Reason: %s",
                    destinationDirectory.getName(), gitUrl, branch, repoSubDirectory, e.getMessage());
            getLogger().error(errorMessage);
            throw new GitException(errorMessage, e);
        } finally {
            clearGitFolder(destinationDirectory);
        }
    }

    private void clearGitFolder(File destinationDirectory) {
        Path pathToClear = Paths.get(destinationDirectory.getAbsolutePath(), ".git");
        FileUtils.clearDirectory(pathToClear);
    }

    protected String extractHeader(final Map<String, String> headers, final String headerName) {
        return headers.keySet().stream()
                .filter(headerName::equalsIgnoreCase)
                .map(headers::get)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("%s header is missing", headerName)));
    }

    protected abstract Logger getLogger();
}
