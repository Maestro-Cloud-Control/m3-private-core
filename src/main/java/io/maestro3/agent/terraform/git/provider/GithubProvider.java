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

import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.terraform.git.GitProviderType;
import io.maestro3.agent.terraform.git.IGitProvider;
import io.maestro3.agent.terraform.git.util.GithubUtils;
import io.maestro3.sdk.internal.util.Assert;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;



@Service
public class GithubProvider extends AbstractGitProvider implements IGitProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GithubProvider.class);


    @Override
    public void partialCloneRepo(String username,
                                 String password,
                                 String githubRepoUri,
                                 String destinationDirectoryPath,
                                 @Nullable String shortBranchName,
                                 @Nullable String hash,
                                 @Nullable String subPath) {
        assertCommonParametersPresent(username, password, githubRepoUri, destinationDirectoryPath, false);

        boolean isTokenAuth = GithubUtils.isTokenAuth(username);
        if (isTokenAuth) {
            username = password;
            password = "";
        }

        partialCloneRepo(username, password, githubRepoUri, destinationDirectoryPath, shortBranchName, hash,
            subPath, isTokenAuth);
    }

    @Override
    public String setupWebhook(String username,
                               String password,
                               String gitRepoUrl,
                               String webhookCallbackUrl,
                               String secret,
                               String branch) throws M3PrivateAgentException {
        validateCommonInputParams(username, password, gitRepoUrl);
        RepositoryService repositoryService = getRepositoryService(username, password);
        try {
            RepositoryHook hook = repositoryService.createHook(
                getUserRepository(repositoryService, gitRepoUrl),
                buildCommonWebHook(webhookCallbackUrl, secret, branch));
            return String.valueOf(hook.getId());
        } catch (IOException ex) {
            String message = String.format(
                "Cannot setup webhook with params: gitRepoUrl = %s, webhookCallbackUrl = %s, " +
                    "secret = %s. Cause: %s",
                gitRepoUrl, webhookCallbackUrl, secret, ex);
            throw new M3PrivateAgentException(message, ex);
        }
    }

    @Override
    public void deleteWebhook(String username,
                              String password,
                              String gitRepoUrl,
                              int id) throws M3PrivateAgentException {
        validateCommonInputParams(username, password, gitRepoUrl);
        Assert.isTrue(id > 0, "Identifier should be > 0.");
        RepositoryService repositoryService = getRepositoryService(username, password);
        try {
            repositoryService.deleteHook(getUserRepository(repositoryService, gitRepoUrl), id);
        } catch (IOException ex) {
            String message = String.format(
                "Cannot delete webhook with params: gitRepoUrl = %s, id = %s. Cause: %s",
                gitRepoUrl, id, ex);
            LOG.error(message, ex);
            throw new M3PrivateAgentException(message, ex);
        }
    }

    @Override
    public GitProviderType getType() {
        return GitProviderType.GITHUB;
    }

    @Override
    public boolean canAuthorize(String username, String password, String gitRepoUri, String branch) throws M3PrivateAgentException {
        return GithubUtils.checkCredentials(username, password);
    }

    @Override
    public void checkRepoExist(String username, String password, String gitUrl) {
        try {
            RepositoryService repositoryService = getRepositoryService(username, password);
            getUserRepository(repositoryService, gitUrl);
        } catch (IOException ex) {
            throw new M3PrivateAgentException(ex);
        }
    }

    private void validateCommonInputParams(String username, String password, String gitRepoUrl) {
        Assert.notNull(username, "Username cannot be null.");
        Assert.notNull(password, "Password cannot be null.");
        Assert.notNull(gitRepoUrl, "Git Repository URL cannot be null.");
    }

    private RepositoryService getRepositoryService(final String username, final String password) {
        GitHubClient gitHubClient = GithubUtils.createGithubClient(username, password);
        return new RepositoryService(gitHubClient);
    }

    private org.eclipse.egit.github.core.Repository getUserRepository(final RepositoryService repositoryService,
                                                                      final String gitRepoUrl) throws IOException {
        return repositoryService.getRepository(GithubUtils.resolveProjectOwner(gitRepoUrl), GithubUtils.resolveProjectName(gitRepoUrl));
    }

    private RepositoryHook buildCommonWebHook(final String webhookCallbackUrl, final String secret, final String branch) {
        RepositoryHook repositoryHook = new RepositoryHook();
        repositoryHook.setName("web");
        repositoryHook.setActive(true);
        Map<String, String> config = new HashMap<>();
        config.put("secret", secret);
        config.put("content_type", "json");
        config.put("url", webhookCallbackUrl);
        config.put("insecure_ssl", "0");
        repositoryHook.setConfig(config);
        return repositoryHook;
    }
}
