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

import io.maestro3.agent.terraform.git.GitProviderType;
import io.maestro3.agent.terraform.git.IGitProvider;
import io.maestro3.sdk.exception.M3SdkException;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.terraform.SdkPrivateAgentGitExecutionResponse;
import io.maestro3.sdk.v3.request.agent.SdkPrivateAgentGitExecutionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GitExecutionHandler extends AbstractM3ApiHandler<SdkPrivateAgentGitExecutionRequest, SdkPrivateAgentGitExecutionResponse> {

    private final Map<GitProviderType, IGitProvider> providerMap;
    @Value("${flag.enable.tf.filepath.replacer:false}")
    private boolean enabledTfFilepathReplacer;
    @Value("${tf.filepath.replace.from:/tf}")
    private String replaceFrom;
    @Value("${tf.filepath.replace.to:/pa}")
    private String replaceTo;

    @Autowired
    public GitExecutionHandler(List<IGitProvider> providers) {
        super(SdkPrivateAgentGitExecutionRequest.class, ActionType.GIT_EXECUTION);
        providerMap = providers.stream().collect(Collectors.toMap(IGitProvider::getType, p -> p));
    }

    @Override
    protected SdkPrivateAgentGitExecutionResponse handlePayload(ActionType action, SdkPrivateAgentGitExecutionRequest request) throws Exception {
        try {
            if (request.getType() == null) {
                throw new IllegalArgumentException("Bad request: request type cannot be null");
            }
            SdkPrivateAgentGitExecutionRequest.Type actionType = request.getType();
            switch (actionType) {
                case CAN_AUTHORIZE:
                    return canAuthorize(request);
                case CHECK_REPO_EXISTS:
                    return checkRepoExists(request);
                case SETUP_WEBHOOK:
                    return setupWebhook(request);
                case DELETE_WEBHOOK:
                    return deleteWebhook(request);
                case RESOLVE_PUSH_HASH:
                    return resolvePushHash(request);
                case PARTIAL_CLONE:
                    return partialClone(request);
                default:
                    throw new IllegalArgumentException("Unsupported request type: " + actionType);
            }
        } catch (Exception ex) {
            LOG.error("Cannot execute action: {}", ex.getMessage(), ex);
            return new SdkPrivateAgentGitExecutionResponse().withError(ex.getMessage());
        }
    }

    private SdkPrivateAgentGitExecutionResponse partialClone(SdkPrivateAgentGitExecutionRequest request) {
        IGitProvider gitProvider = resolveGitProvider(request);
        gitProvider.partialCloneRepo(request.getUsername(), request.getPassword(), request.getGitRepoUri(),
            replace(request.getDestinationDirectoryPath()), request.getShortBranchName(), request.getHash(), request.getSubPath());
        return new SdkPrivateAgentGitExecutionResponse().withPartialCloneRepo(true);
    }

    private String replace(String path) {
        return enabledTfFilepathReplacer
            ? path.replaceAll(replaceFrom, replaceTo)
            : path;
    }

    private SdkPrivateAgentGitExecutionResponse resolvePushHash(SdkPrivateAgentGitExecutionRequest request) {
        IGitProvider gitProvider = resolveGitProvider(request);
        String pushHash = gitProvider.resolvePushHash(request.getHash());
        return new SdkPrivateAgentGitExecutionResponse().withResolvePushHash(pushHash);
    }

    private SdkPrivateAgentGitExecutionResponse deleteWebhook(SdkPrivateAgentGitExecutionRequest request) {
        IGitProvider gitProvider = resolveGitProvider(request);
        gitProvider.deleteWebhook(request.getUsername(), request.getPassword(), request.getGitRepoUri(), request.getHookId());
        return new SdkPrivateAgentGitExecutionResponse().withDeleteWebhook(true);
    }

    private SdkPrivateAgentGitExecutionResponse setupWebhook(SdkPrivateAgentGitExecutionRequest request) {
        IGitProvider gitProvider = resolveGitProvider(request);
        String webhook = gitProvider.setupWebhook(request.getUsername(), request.getPassword(), request.getGitRepoUri(), request.getWebhookCallbackUrl(), request.getSecret(), request.getShortBranchName());
        return new SdkPrivateAgentGitExecutionResponse().withSetupWebhook(webhook);
    }

    private SdkPrivateAgentGitExecutionResponse checkRepoExists(SdkPrivateAgentGitExecutionRequest request) {
        IGitProvider gitProvider = resolveGitProvider(request);
        gitProvider.checkRepoExist(request.getUsername(), request.getPassword(), request.getGitRepoUri());
        return new SdkPrivateAgentGitExecutionResponse().withCheckRepoExist(true);
    }

    private SdkPrivateAgentGitExecutionResponse canAuthorize(SdkPrivateAgentGitExecutionRequest request) {
        IGitProvider gitProvider = resolveGitProvider(request);
        boolean result = gitProvider.canAuthorize(request.getUsername(), request.getPassword(), request.getGitRepoUri(), request.getShortBranchName());
        return new SdkPrivateAgentGitExecutionResponse().withCanAuthorize(result);
    }

    private IGitProvider resolveGitProvider(SdkPrivateAgentGitExecutionRequest request) {
        SdkPrivateAgentGitExecutionRequest.GitType gitType = request.getGitType();
        if (gitType == null) {
            throw new M3SdkException("Unsupported git type: " + gitType);
        }
        GitProviderType gitProviderType;
        switch (gitType) {
            case GITLAB:
                gitProviderType = GitProviderType.GITLAB;
                break;
            case GITHUB:
                gitProviderType = GitProviderType.GITHUB;
                break;
            default:
                throw new M3SdkException("Unsupported git type: " + gitType);
        }
        IGitProvider gitProvider = providerMap.get(gitProviderType);
        if (gitProvider == null) {
            throw new M3SdkException("Git provider not found: " + gitType);
        }
        return gitProvider;
    }

    @Override
    public SdkCloud getSupportedCloud() {
        // all clouds
        return null;
    }
}
