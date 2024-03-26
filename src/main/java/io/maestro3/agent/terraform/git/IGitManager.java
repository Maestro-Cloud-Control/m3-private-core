package io.maestro3.agent.terraform.git;

import java.io.File;
import java.util.Map;

public interface IGitManager {
    void validateStorageInfo(String url, String token, String branch, String subDirectory);

    void clone(char[] token, String gitUrl, File destinationDirectory, String branch, String repoSubDirectory);

    long setupWebhook(String templateUuid, String callbackUrl, String token, String repositoryUrl, String branch);

    void verifyWebhookCallbackIssuer(String templateUuid, String payload, Map<String, String> headers);

    boolean validateEvent(String templateUuid, Map<String, String> headers);

    void updateWebhook(String templateUuid, String repositoryUrl, String webhookId, String newBranch);

    void deleteWebhook(String templateUuid, String repositoryUrl, String webhookId);

}
