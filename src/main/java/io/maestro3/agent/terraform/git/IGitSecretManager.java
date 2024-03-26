package io.maestro3.agent.terraform.git;

import java.util.Optional;

public interface IGitSecretManager {

    void createSecret(String templateUuid, char[] secret);

    Optional<char[]> getSecret(String templateUuid);

    void deleteSecret(String templateUuid);
}
