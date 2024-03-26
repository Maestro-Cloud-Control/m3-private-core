package io.maestro3.agent.terraform.git.impl;

import io.maestro3.agent.terraform.git.IGitWebhookSecretManager;
import io.maestro3.agent.terraform.git.util.HashUtils;
import io.maestro3.agent.terraform.git.util.RandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Service
public class GitWebhookSecretManager extends BaseSecretManager<GitWebhookSecretManager.GitWebhookSecret> implements IGitWebhookSecretManager {

    private static final String MISSING_SECRET_ERROR_MESSAGE = "Secret for verification not found; identifier: %s";

    private final RandomGenerator randomGenerator;

    @Autowired
    public GitWebhookSecretManager(@Value("${vault.kv.engine.path}") String secretEnginePath,
                                   VaultTemplate vaultTemplate,
                                   RandomGenerator randomGenerator) {
        super(GitWebhookSecret.class, secretEnginePath, vaultTemplate, "git/webhook/");
        this.randomGenerator = randomGenerator;
    }

    @Override
    public String generateAndSaveToken(final String identifier) {
        final String randomToken = randomGenerator.generate(RandomGenerator.Format.HEX);
        final GitWebhookSecret gitWebhookSecret = new GitWebhookSecret();
        gitWebhookSecret.setToken(randomToken);
        super.create(identifier, gitWebhookSecret);
        return randomToken;
    }

    @Override
    public boolean verify(final String identifier, final String payload, final String providedHash) throws NoSuchAlgorithmException, InvalidKeyException {
        final GitWebhookSecret gitWebhookSecret = super.get(identifier)
                .orElseThrow(() -> new IllegalStateException(String.format(MISSING_SECRET_ERROR_MESSAGE, identifier)));
        final String generatedHash = HashUtils.hmacHex(HashUtils.HMAC_SHA256_ALGORITHM, gitWebhookSecret.getToken(), payload);
        return Objects.equals(generatedHash, providedHash);
    }

    @Override
    public boolean verify(final String identifier, final String providedSecret) {
        final GitWebhookSecret webhookSecret = super.get(identifier)
                .orElseThrow(() -> new IllegalStateException(String.format(MISSING_SECRET_ERROR_MESSAGE, identifier)));
        return Objects.equals(webhookSecret.token, providedSecret);
    }

    @Override
    public void deleteSecret(final String identifier) {
        super.delete(identifier);
    }

    public static final class GitWebhookSecret {
        private String token;

        public String getToken() {
            return token;
        }

        private void setToken(String token) {
            this.token = token;
        }
    }
}
