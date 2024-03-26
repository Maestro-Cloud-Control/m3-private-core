package io.maestro3.agent.terraform.git.impl;

import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.support.Versioned;

import java.util.Optional;

public abstract class BaseSecretManager<R> {

    private final Class<R> responseClass;
    private final VaultVersionedKeyValueOperations operations;
    private final String folder;

    protected BaseSecretManager(Class<R> responseClass, String secretEnginePath, VaultTemplate vaultTemplate) {
        this.responseClass = responseClass;
        this.operations = vaultTemplate.opsForVersionedKeyValue(secretEnginePath);
        this.folder = null;
    }

    protected BaseSecretManager(Class<R> responseClass, String secretEnginePath, VaultTemplate vaultTemplate,
                                String folder) {
        this.responseClass = responseClass;
        this.operations = vaultTemplate.opsForVersionedKeyValue(secretEnginePath);
        this.folder = folder;
    }

    protected void create(String identifier, R secret) {
        String path = buildSecretPath(identifier);
        operations.put(path, secret);
    }

    protected Optional<R> get(String identifier) {
        String path = buildSecretPath(identifier);
        Versioned<R> response = operations.get(path, responseClass);
        return Optional.ofNullable(response)
                .map(Versioned::getData);
    }

    protected void delete(String identifier) {
        String path = buildSecretPath(identifier);
        operations.opsForKeyValueMetadata().delete(path);
    }

    private String buildSecretPath(String identifier) {
        return Optional.ofNullable(folder).map(s -> String.join("/", s, identifier)).orElse(identifier);
    }
}
