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

package io.maestro3.agent.terraform.git.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.terraform.git.IGitProvider;
import io.maestro3.sdk.internal.util.Assert;
import io.maestro3.sdk.internal.util.JsonUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;



public abstract class AbstractGitProvider implements IGitProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractGitProvider.class);

    protected void partialCloneRepo(String username,
                                    String password,
                                    String gitRepoUri,
                                    String destinationDirectoryPath,
                                    @Nullable String shortBranchName,
                                    @Nullable String hash,
                                    @Nullable String subPath,
                                    boolean isTokenAuth) throws M3PrivateAgentException {

        assertCommonParametersPresent(username, password, gitRepoUri, destinationDirectoryPath, isTokenAuth);

        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
        CloneCommand cloneCommand = Git.cloneRepository()
            .setURI(gitRepoUri)
            .setDirectory(new File(destinationDirectoryPath))
            .setNoCheckout(true)
            .setCredentialsProvider(credentialsProvider);

        if (StringUtils.isNotBlank(shortBranchName)) {
            cloneCommand.setBranchesToClone(Collections.singletonList(shortBranchName));
            cloneCommand.setBranch(shortBranchName);
        }

        try (Git gitRepo = cloneCommand.call()) {
            String branch = "origin/" + (StringUtils.isNotBlank(shortBranchName)
                ? shortBranchName
                : gitRepo.getRepository().getBranch());

            String startPoint = StringUtils.isNotBlank(hash) ? hash : branch;

            CheckoutCommand checkoutCommand = gitRepo.checkout()
                .setName(branch)
                .setStartPoint(startPoint);

            if (StringUtils.isNotBlank(subPath)) {
                checkoutCommand.addPath(subPath);
            } else {
                checkoutCommand.setAllPaths(true);
            }

            checkoutCommand.call();
            gitRepo.getRepository().close();
        } catch (GitAPIException | IOException ex) {
            String message = String.format(
                "Cannot clone repo with params: githubUri = %s, destinationDirectoryPath = %s, " +
                    "branch = %s, subPath = %s, hash = %s. Cause: %s",
                gitRepoUri, destinationDirectoryPath, shortBranchName, subPath, hash, ex);
            LOG.error(message);
            throw new M3PrivateAgentException(message, ex);
        }
    }

    @Override
    public String resolvePushHash(String gitWebHookJson) {
        Assert.hasText(gitWebHookJson, "gitWebHookJson cannot be null.");
        Map<String, Object> rootNode = JsonUtils.parseJson(gitWebHookJson, new TypeReference<Map<String, Object>>() {
        });
        return (String) rootNode.get("after");
    }

    protected void assertCommonParametersPresent(@Nonnull String username, @Nonnull String password,
                                                 @Nonnull String gitRepoUri, @Nonnull String destinationDirectoryPath,
                                                 boolean isTokenAuth) {
        Assert.hasText(username, "username cannot be null or empty");
        if (!isTokenAuth) {
            Assert.hasText(password, "password cannot be null or empty");
        }
        Assert.hasText(gitRepoUri, "gitRepoUri cannot be null or empty");
        Assert.hasText(destinationDirectoryPath, "destinationDirectoryPath cannot be null or empty");
    }
}
