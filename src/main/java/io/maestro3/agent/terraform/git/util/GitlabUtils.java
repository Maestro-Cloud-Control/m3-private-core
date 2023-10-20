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

package io.maestro3.agent.terraform.git.util;

import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.sdk.internal.util.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.StringJoiner;


public final class GitlabUtils {
    private static final Logger LOG = LoggerFactory.getLogger(GitlabUtils.class);
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final String INCORRECT_GIT_URL_URL = "Incorrect git url. url -> ";

    private GitlabUtils() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }

    public static boolean checkCredentials(String username, String password, String gitUrl) {
        try (GitLabApi c = createGitlabClient(username, password, gitUrl)) {
            return true;
        } catch (Exception e) {
            LOG.error("Cannot check credentials. Cause: {}", e.getMessage(), e);
            return false;
        }
    }

    public static GitLabApi createGitlabClient(String username,
                                               String password,
                                               String gitUrl) {
        String host = resolveHost(gitUrl);
        if (GithubUtils.isTokenAuth(username)) {
            return new GitLabApi(host, password);
        } else {
            try {
                return GitLabApi.oauth2Login(host, username, password);
            } catch (GitLabApiException e) {
                LOG.error("Cannot connect to Gitlab: {}", e.getMessage(), e);
                throw new M3PrivateAgentException("Cannot connect to Gitlab.");
            }
        }
    }

    private static boolean isUrlValid(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        try {
            URL u = new URL(url); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            // skip exception, we just need to check the validity
            return false;
        }
    }

    public static String[] getGitUrlParams(String gitUrl) {
        if (!isUrlValid(gitUrl) || gitUrl.length() <= 10) {
            throw new IllegalArgumentException(INCORRECT_GIT_URL_URL + gitUrl);
        }
        String port;
        String url;
        if (gitUrl.startsWith(HTTP)) {
            url = gitUrl.substring(7);
            port = HTTP;
        } else if (gitUrl.startsWith(HTTPS)) {
            url = gitUrl.substring(8);
            port = HTTPS;
        } else {
            throw new IllegalArgumentException(INCORRECT_GIT_URL_URL + gitUrl);
        }
        String[] parts = url.split("/");
        if (parts.length < 3) {
            throw new IllegalArgumentException(INCORRECT_GIT_URL_URL + gitUrl);
        }
        for (String part : parts) {
            if (StringUtils.isBlank(part)) {
                throw new IllegalArgumentException(INCORRECT_GIT_URL_URL + gitUrl);
            }
        }
        String[] allParts = new String[parts.length + 1];
        allParts[0] = port;
        System.arraycopy(parts, 0, allParts, 1, parts.length);
        return allParts;
    }

    public static String resolveHost(String gitUrl) {
        return getGitUrlParams(gitUrl)[0] + getGitUrlParams(gitUrl)[1];
    }

    public static String resolveProjectName(String gitUrl) {
        String[] gitUrlParams = getGitUrlParams(gitUrl);
        return gitUrlParams[gitUrlParams.length - 1].replace(".git", "");
    }

    public static String resolveProjectOwner(String gitUrl) {
        String[] gitUrlParams = getGitUrlParams(gitUrl);
        StringJoiner stringJoiner = new StringJoiner("/");
        for (int i = 2; i < gitUrlParams.length - 1; i++) {
            stringJoiner.add(gitUrlParams[i]);
        }
        return stringJoiner.toString();
    }

    public static String resolveFullProjectPath(String gitUrl) {
        return resolveProjectOwner(gitUrl) + "/" + resolveProjectName(gitUrl);
    }
}
