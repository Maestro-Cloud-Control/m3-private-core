package io.maestro3.agent.terraform.git;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface IGitWebhookSecretManager {
    String generateAndSaveToken(String identifier);

    boolean verify(String identifier, String payload, String providedHash) throws NoSuchAlgorithmException, InvalidKeyException;

    boolean verify(String identifier, String providedSecret);

    void deleteSecret(String identifier);
}
