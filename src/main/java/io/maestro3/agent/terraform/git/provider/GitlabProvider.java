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
import io.maestro3.agent.terraform.git.util.GitlabUtils;
import io.maestro3.sdk.internal.util.Assert;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.ProjectHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;



@Service
public class GitlabProvider extends AbstractGitProvider implements IGitProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GitlabProvider.class);

    @Override
    public void partialCloneRepo(String username,
                                 String password,
                                 String gitRepoUri,
                                 String destinationDirectoryPath,
                                 @Nullable String shortBranchName,
                                 @Nullable String hash,
                                 @Nullable String subPath) {
        assertCommonParametersPresent(username, password, gitRepoUri, destinationDirectoryPath, false);

        boolean isTokenAuth = GithubUtils.isTokenAuth(username);
        if (isTokenAuth) {
            username = "username";
            gitRepoUri = resolveTokenizedUrl(gitRepoUri, password);
        }

        partialCloneRepo(username, password, gitRepoUri, destinationDirectoryPath, shortBranchName,
            hash, subPath, isTokenAuth);
    }

    private String resolveTokenizedUrl(String gitlabRepoUri, String password) {
        String domain = getDomainName(gitlabRepoUri);
        String domainReplacement = "gitlab-ci-token:" + password + "@" + domain;
        return gitlabRepoUri.replaceFirst(domain, domainReplacement);
    }

    private static String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.")
                ? domain.substring(4)
                : domain;
        } catch (URISyntaxException e) {
            throw new M3PrivateAgentException("Cannot resolve domain for URL: " + url);
        }
    }

    @Override
    public String setupWebhook(String username,
                               String password,
                               String gitUrl,
                               String webhookCallbackUrl,
                               String secret,
                               String branch) throws M3PrivateAgentException {
        validateCommonInputParams(username, password, gitUrl);

        try (GitLabApi gitLabApi = GitlabUtils.createGitlabClient(username, password, gitUrl)) {
            ProjectHook enabledHooks = new ProjectHook().withPushEvents(true).withPushEventsBranchFilter(branch);
            ProjectHook hook = gitLabApi.getProjectApi().addHook(GitlabUtils.resolveFullProjectPath(gitUrl), webhookCallbackUrl, enabledHooks, false, secret);
            return String.valueOf(hook.getId());
        } catch (GitLabApiException ex) {
            String message = String.format(
                "Cannot setup webhook with params: gitUrl = %s, webhookCallbackUrl = %s, " +
                    "secret = %s. Cause: %s",
                gitUrl, webhookCallbackUrl, secret, ex);
            LOG.error(message, ex);
            throw new M3PrivateAgentException(message, ex);
        }
    }

    @Override
    public void deleteWebhook(String username,
                              String password,
                              String gitUrl,
                              int id) throws M3PrivateAgentException {
        validateCommonInputParams(username, password, gitUrl);
        Assert.isTrue(id > 0, "Identifier should be > 0.");

        try (GitLabApi gitLabApi = GitlabUtils.createGitlabClient(username, password, gitUrl)) {
            gitLabApi.getProjectApi().deleteHook(GitlabUtils.resolveFullProjectPath(gitUrl), id);
        } catch (GitLabApiException ex) {
            String message = String.format(
                "Cannot delete webhook with params: gitUrl = %s, id = %s. Cause: %s",
                gitUrl, id, ex);
            LOG.error(message, ex);
            throw new M3PrivateAgentException(message, ex);
        }
    }

    @Override
    public GitProviderType getType() {
        return GitProviderType.GITLAB;
    }

    @Override
    public boolean canAuthorize(String username, String password, String gitRepoUrl, String branch) throws M3PrivateAgentException {
        return GitlabUtils.checkCredentials(username, password, gitRepoUrl);
    }

    @Override
    public void checkRepoExist(String username, String password, String gitUrl) {
        validateCommonInputParams(username, password, gitUrl);

        try (GitLabApi gitLabApi = GitlabUtils.createGitlabClient(username, password, gitUrl)) {
            gitLabApi.getProjectApi().getProject(GitlabUtils.resolveFullProjectPath(gitUrl));
        } catch (GitLabApiException e) {
            LOG.error("Project is not found by url: {}", gitUrl, e);
            throw new M3PrivateAgentException("Project is not found by url", e);
        }
    }

    private void validateCommonInputParams(String username, String password, String projectName) {
        Assert.notNull(username, "Username cannot be null.");
        Assert.notNull(password, "Password cannot be null.");
        Assert.notNull(projectName, "Project name cannot be null.");
    }

}
