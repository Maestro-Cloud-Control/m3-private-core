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
import io.maestro3.agent.terraform.git.exception.GitAuthException;
import io.maestro3.agent.terraform.git.exception.GitException;
import io.maestro3.agent.terraform.git.exception.GitResourceNotFoundException;
import io.maestro3.agent.terraform.git.util.GitUtils;
import io.maestro3.agent.terraform.git.util.ParsedGitUrl;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.apache.http.util.Asserts;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProjectHook;
import org.gitlab4j.api.models.TreeItem;
import org.gitlab4j.api.webhook.PushEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class GitLabManager extends AbstractGitManager implements IGitManager {

    private static final Logger LOG = LoggerFactory.getLogger(GitLabManager.class);

    private static final String INVALID_URL_ERROR_MESSAGE = "Invalid GitLab URL. %s";
    private static final String GITLAB_TOKEN_HEADER = "x-gitlab-token";
    private static final String GITLAB_EVENT_HEADER = "x-gitlab-event";

    public GitLabManager(IGitSecretManager secretManager,
                         IGitWebhookSecretManager webhookSecretManager,
                         final String providerName) {
        super(secretManager, webhookSecretManager, providerName);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public void validateStorageInfo(final String url, final String token, final String branch, final String subDirectory) {
        Asserts.notBlank(url, "git URL");
        Asserts.notBlank(token, "git token");
        Asserts.notBlank(branch, "git branch");

        ParsedGitUrl parsedGitUrl = GitUtils.parseUrl(url, providerName);
        try (GitLabApi api = new GitLabApi(parsedGitUrl.getDomainUrl(), token)) {
            Project project = api.getProjectApi().getProject(parsedGitUrl.getGroupName(), parsedGitUrl.getRepositoryName());
            Branch gitBranch = api.getRepositoryApi().getBranch(project, branch);
            if (StringUtils.isNotBlank(subDirectory)) {
                List<TreeItem> tree = api.getRepositoryApi().getTree(project, subDirectory, gitBranch.getName());
                if (CollectionUtils.isEmpty(tree)) {
                    throw new GitException(String.format("Directory %s is empty", subDirectory));
                }
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public long setupWebhook(final String templateUuid, final String callbackUrl,
                             final String token, final String repositoryUrl, final String branch) {
        final ParsedGitUrl parsedGitUrl = GitUtils.parseUrl(repositoryUrl, providerName);
        boolean webhookSecretCreated = false;
        try (GitLabApi api = new GitLabApi(parsedGitUrl.getDomainUrl(), token)) {
            final Project project = api.getProjectApi().getProject(parsedGitUrl.getGroupName(), parsedGitUrl.getRepositoryName());
            final String webhookToken = webhookSecretManager.generateAndSaveToken(templateUuid);
            webhookSecretCreated = true;
            final ProjectHook enabledHooks = new ProjectHook().withPushEvents(true).withPushEventsBranchFilter(branch);
            final ProjectHook createdHook = api.getProjectApi().addHook(project, callbackUrl, enabledHooks,
                    false, webhookToken);
            return createdHook.getId();
        } catch (Exception e) {
            throw handleException(e, templateUuid, webhookSecretCreated);
        }
    }

    @Override
    public void verifyWebhookCallbackIssuer(final String templateUuid, final String payload,
                                            final Map<String, String> headers) {
        final String tokenHeader = extractHeader(headers, GITLAB_TOKEN_HEADER);
        boolean valid = webhookSecretManager.verify(templateUuid, tokenHeader);
        if (!valid) {
            throw new IllegalStateException("Webhook callback issuer verification failed");
        }
    }

    @Override
    public boolean validateEvent(final String templateUuid, final Map<String, String> headers) {
        final String eventHeader = extractHeader(headers, GITLAB_EVENT_HEADER);
        if (PushEvent.X_GITLAB_EVENT.equalsIgnoreCase(eventHeader)) {
            return true;
        }
        throw new IllegalArgumentException(String.format("Got unknown event %s in webhook callback for template %s",
                eventHeader, templateUuid));
    }

    @Override
    public void updateWebhook(final String templateUuid, final String repositoryUrl,
                              final String webhookId, final String newBranch) {
        final ParsedGitUrl parsedGitUrl = GitUtils.parseUrl(repositoryUrl, providerName);
        final String token = secretManager.getSecret(templateUuid)
                .map(String::new)
                .orElseThrow();
        try (GitLabApi api = new GitLabApi(parsedGitUrl.getDomainUrl(), token)) {
            ProjectHook hook = api.getProjectApi().getHook(parsedGitUrl.getFullRepositoryName(), Integer.valueOf(webhookId));
            hook.setPushEventsBranchFilter(newBranch);
            api.getProjectApi().modifyHook(hook);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void deleteWebhook(final String templateUuid, final String repositoryUrl, final String webhookId) {
        final ParsedGitUrl parsedGitUrl = GitUtils.parseUrl(repositoryUrl, providerName);
        final String token = secretManager.getSecret(templateUuid)
                .map(String::new)
                .orElseThrow();
        try (GitLabApi api = new GitLabApi(parsedGitUrl.getDomainUrl(), token)) {
            api.getProjectApi().deleteHook(parsedGitUrl.getFullRepositoryName(), Integer.valueOf(webhookId));
        } catch (Exception e) {
            throw handleException(e);
        } finally {
            webhookSecretManager.deleteSecret(templateUuid);
        }
    }

    private GitException handleException(final Exception e, final String templateId, final boolean deleteWebhookSecret) {
        if (deleteWebhookSecret) {
            webhookSecretManager.deleteSecret(templateId);
        }
        return handleException(e);
    }

    private GitException handleException(Exception e) {
        if (e instanceof GitLabApiException) {
            return handleException((GitLabApiException) e);
        }
        if (e instanceof URISyntaxException) {
            return new GitException(String.format(INVALID_URL_ERROR_MESSAGE, e.getMessage()));
        }
        return new GitException(e.getMessage(), e);
    }

    private GitException handleException(final GitLabApiException e) {
        final int httpStatus = e.getHttpStatus();
        if (httpStatus == HttpURLConnection.HTTP_UNAUTHORIZED || httpStatus == HttpURLConnection.HTTP_FORBIDDEN) {
            return new GitAuthException(String.format("Invalid GitLab credentials: %s", e.getMessage()));
        }
        if (httpStatus == HttpURLConnection.HTTP_NOT_FOUND) {
            return new GitResourceNotFoundException(e.getMessage());
        }
        return new GitException(e.getMessage());
    }

}
