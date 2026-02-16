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

import io.maestro3.sdk.internal.util.StringUtils;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


public final class GithubUtils {

    public static final String TOKEN = "TOKEN";

    private GithubUtils() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }

    public static boolean checkCredentials(String username, String password) {
        try {
            GitHubClient client = createGithubClient(username, password);
            client.get(new GitHubRequest().setUri("/user"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static GitHubClient createGithubClient(String username, String password) {
        GitHubClient client = new GitHubClient();

        if (isTokenAuth(username)) {
            client.setOAuth2Token(password);
        } else {
            client.setCredentials(username, password);
        }

        return client;
    }

    public static boolean isTokenAuth(String username) {
        return StringUtils.isEqualsIgnoreCase(TOKEN, username);
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
        if (isUrlValid(gitUrl) && gitUrl.startsWith("https://github.com/") && gitUrl.length() > 19) {
            String url = gitUrl.substring(8);
            String[] parts = url.split("/");
            if (parts.length == 3) {
                boolean valid = true;
                for (String part : parts) {
                    if (StringUtils.isBlank(part)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    return parts;
                }
            }
        }
        throw new IllegalArgumentException("Incorrect git url. url -> " + gitUrl);
    }

    public static String resolveHost(String gitUrl) {
        return getGitUrlParams(gitUrl)[0];
    }

    public static String resolveProjectName(String gitUrl) {
        return getGitUrlParams(gitUrl)[2].replace(".git", "");
    }

    public static String resolveProjectOwner(String gitUrl) {
        return getGitUrlParams(gitUrl)[1];
    }

    public static String resolveFullProjectPath(String gitUrl) {
        return resolveProjectOwner(gitUrl) + "/" + resolveProjectName(gitUrl);
    }

}
