package io.maestro3.agent.terraform.git.impl;

import io.maestro3.agent.terraform.git.IGitSecretManager;
import io.maestro3.agent.terraform.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;

import java.util.Optional;

@Service
public class GitSecretManager extends BaseSecretManager<GitSecretManager.GitSecret> implements IGitSecretManager {

    @Autowired
    public GitSecretManager(@Value("${vault.kv.engine.path}") String secretEnginePath,
                            VaultTemplate vaultTemplate) {
        super(GitSecret.class, secretEnginePath, vaultTemplate, "git/template/repository");
    }

    @Override
    public void createSecret(String templateUuid, char[] secret) {
        GitSecret gitSecret = new GitSecret();
        gitSecret.setToken(secret);
        super.create(templateUuid, gitSecret);
        SecurityUtils.clearSensitiveData(secret);
    }

    @Override
    public Optional<char[]> getSecret(String templateUuid) {
        return super.get(templateUuid)
                .map(GitSecret::getToken);
    }

    @Override
    public void deleteSecret(String templateUuid) {
        super.delete(templateUuid);
    }


    public static final class GitSecret {
        private char[] token;

        public char[] getToken() {
            return token;
        }

        private void setToken(char[] token) {
            this.token = token;
        }
    }
}
