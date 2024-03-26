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

import io.maestro3.agent.terraform.git.IGitSecretManager;
import io.maestro3.agent.terraform.git.IGitWebhookSecretManager;
import io.maestro3.agent.terraform.git.exception.GitAuthException;
import io.maestro3.agent.terraform.git.exception.GitException;
import io.maestro3.agent.terraform.git.exception.GitResourceNotFoundException;
import io.maestro3.agent.terraform.git.util.GitUtils;
import io.maestro3.agent.terraform.git.util.ParsedGitUrl;
import io.maestro3.sdk.internal.util.StringUtils;
import org.apache.http.util.Asserts;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubManager extends AbstractGitManager {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubManager.class);

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid GitHub credentials";
    private static final String SIGNATURE_HEADER = "X-Hub-Signature-256";
    private static final String EVENT_HEADER = "X-GitHub-Event";
    private static final Pattern SIGNATURE_PATTERN = Pattern.compile("^sha256=([0-9a-f]{64})$");

    public GitHubManager(IGitSecretManager secretManager,
                         IGitWebhookSecretManager webhookSecretManager,
                         final String providerName) {
        super(secretManager, webhookSecretManager, providerName);
    }

    @Override
    public void validateStorageInfo(String url, String token, String branch, String subDirectory) {
        Asserts.notBlank(url, "git URL");
        Asserts.notBlank(token, "git token");
        Asserts.notBlank(branch, "git branch");

        ParsedGitUrl parsedGitUrl = GitUtils.parseUrl(url, providerName);
        GitHub gitHub = validateToken(token);
        String repositoryName = parsedGitUrl.getFullRepositoryName();
        GHRepository repository = getRepository(gitHub, repositoryName);
        assertBranchExists(repository, branch);
        if (StringUtils.isNotBlank(subDirectory)) {
            assertSubDirectoryExists(repository, branch, subDirectory);
        }
    }

    private GitHub validateToken(String token) {
        try {
            GitHub gitHub = new GitHubBuilder().withOAuthToken(token).build();
            gitHub.checkApiUrlValidity();
            return gitHub;
        } catch (IOException e) {
            LOG.error(INVALID_CREDENTIALS_MESSAGE, e);
            throw new GitAuthException(INVALID_CREDENTIALS_MESSAGE);
        }
    }

    private GHRepository getRepository(GitHub gitHub, String repositoryName) {
        try {
            return gitHub.getRepository(repositoryName);
        } catch (FileNotFoundException e) {
            throw new GitResourceNotFoundException(String.format("Repository %s not found", repositoryName));
        } catch (IOException e) {
            String errorMessage = String.format("Failed to get repository %s. Reason: %s", repositoryName, e.getMessage());
            LOG.error(errorMessage, e);
            throw new GitException(errorMessage);
        }
    }

    private void assertBranchExists(GHRepository repository, String branch) {
        try {
            repository.getBranch(branch);
        } catch (FileNotFoundException e) {
            throw new GitResourceNotFoundException(String.format("Branch %s not found", branch));
        } catch (IOException e) {
            String errorMessage = String.format("Failed to get branch %s. Reason: %s", branch, e.getMessage());
            LOG.error(errorMessage, e);
            throw new GitException(errorMessage);
        }
    }

    private void assertSubDirectoryExists(GHRepository repository, String branch, String subDirectory) {
        try {
            List<GHContent> directoryContent = repository.getDirectoryContent(subDirectory, branch);
            if (directoryContent.isEmpty()) {
                throw new GitException(String.format("Directory %s is empty", subDirectory));
            }
        } catch (FileNotFoundException e) {
            throw new GitResourceNotFoundException(String.format("Directory %s not found", subDirectory));
        } catch (IOException e) {
            String errorMessage = String.format("Failed to get directory %s content. Reason: %s", subDirectory, e.getMessage());
            LOG.error(errorMessage, e);
            throw new GitException(errorMessage);
        }
    }

    @Override
    public long setupWebhook(String templateUuid, String callbackUrl, String token, String repositoryUrl, String branch) {
        GitHub gitHub = validateToken(token);
        ParsedGitUrl parsedGitUrl = GitUtils.parseUrl(repositoryUrl, providerName);
        GHRepository repository = getRepository(gitHub, parsedGitUrl.getFullRepositoryName());
        String webhookToken = webhookSecretManager.generateAndSaveToken(templateUuid);
        Map<String, String> config = Map.of(
                "url", callbackUrl,
                "content_type", "json",
                "secret", webhookToken,
                "insecure_ssl", "0"
        );
        try {
            GHHook webhook = repository.createHook("web", config, Collections.singleton(GHEvent.PUSH), true);
            return webhook.getId();
        } catch (IOException e) {
            webhookSecretManager.deleteSecret(templateUuid);
            throw new GitException(e.getMessage(), e);
        }
    }

    @Override
    public void verifyWebhookCallbackIssuer(String templateUuid, String payload, Map<String, String> headers) {
        String providedHash = extractProvidedHash(headers);
        try {
            boolean valid = webhookSecretManager.verify(templateUuid, payload, providedHash);
            if (!valid) {
                throw new IllegalStateException("Webhook callback issuer verification failed");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new GitException(String.format("Failed to verify issuer. Reason: %s", e.getMessage()));
        }
    }

    private String extractProvidedHash(Map<String, String> headers) {
        String signatureHeader = extractHeader(headers, SIGNATURE_HEADER);
        Matcher matcher = SIGNATURE_PATTERN.matcher(signatureHeader);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Got malformed signature: " + signatureHeader);
        }
        return matcher.group(1);
    }

    @Override
    public boolean validateEvent(String templateUuid, Map<String, String> headers) {
        String eventHeader = extractHeader(headers, EVENT_HEADER);
        if (GHEvent.PING.name().equalsIgnoreCase(eventHeader)) {
            return false;
        }
        if (GHEvent.PUSH.name().equalsIgnoreCase(eventHeader)) {
            return true;
        }
        throw new IllegalArgumentException(String.format("Got unknown event %s", eventHeader));
    }

    @Override
    public void updateWebhook(String templateUuid, String repositoryUrl, String webhookId, String newBranch) {
        // do nothing, github does not support hook branch filtering
    }

    @Override
    public void deleteWebhook(final String templateUuid, final String repositoryUrl, final String webhookId) {
        final int hookId = Integer.parseInt(webhookId);
        final GitHub gitHub = getGitHubClient(templateUuid);
        final ParsedGitUrl parsedGitUrl = GitUtils.parseUrl(repositoryUrl, providerName);
        final GHRepository repository = getRepository(gitHub, parsedGitUrl.getFullRepositoryName());

        try {
            repository.deleteHook(hookId);
        } catch (IOException e) {
            throw new GitException("Failed to delete webhook for template " + templateUuid, e);
        } finally {
            webhookSecretManager.deleteSecret(templateUuid);
        }
    }

    private GitHub getGitHubClient(final String templateUuid) {
        return secretManager.getSecret(templateUuid)
                .map(String::new)
                .map(this::validateToken)
                .orElseThrow();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
