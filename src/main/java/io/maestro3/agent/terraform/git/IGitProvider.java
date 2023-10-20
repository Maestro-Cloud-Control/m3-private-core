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

package io.maestro3.agent.terraform.git;

import io.maestro3.agent.exception.M3PrivateAgentException;

import javax.annotation.Nullable;


public interface IGitProvider {

    /**
     * Clone the Github repo with provided data.
     * <br/>
     * <b>Note:</b> if shortBranchName, hash and subpath are empty, then full repo will be cloned with default branch
     * <br/>
     * <b>Note:</b> if <code>hash</code> is used, then shortBranchName is ignored
     *
     * @param username                 - the github username <b>(required)</b>
     * @param password                 - the user password <b>(required)</b>
     * @param gitRepoUri               - the URI of Git repo to be cloned <b>(required)</b>
     * @param destinationDirectoryPath - the local destination directory path to clone the repo <b>(required)</b>
     * @param shortBranchName          - the short name of the branch to be cloned without origin/... or refs/...
     *                                 If empty, then default branch os used.<b>(optional)</b>
     * @param hash                     - the hash tag of revision to be cloned (if hash is empty, then ignored.
     *                                 If non-empty, then shortBranchName is ignored) <b>(optional)</b>
     * @param subPath                  - the subfolder to be cloned (sparse checkout).
     *                                 If empty then full repo will be cloned <b>(optional)</b>
     * @throws M3PrivateAgentException if cannot clone the repo
     */
    void partialCloneRepo(String username,
                          String password,
                          String gitRepoUri,
                          String destinationDirectoryPath,
                          @Nullable String shortBranchName,
                          @Nullable String hash,
                          @Nullable String subPath) throws M3PrivateAgentException;

    String setupWebhook(String username,
                        String password,
                        String gitRepoUri,
                        String webhookCallbackUrl,
                        @Nullable String secret,
                        String branch) throws M3PrivateAgentException;

    void deleteWebhook(String username,
                       String password,
                       String gitRepoUri,
                       int id) throws M3PrivateAgentException;

    GitProviderType getType();

    String resolvePushHash(String gitWebHookJson);

    boolean canAuthorize(String username, String password, String gitRepoUri, String branch) throws M3PrivateAgentException;

    void checkRepoExist(String username, String password, String gitUrl);
}
