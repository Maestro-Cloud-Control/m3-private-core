package io.maestro3.agent.terraform.git.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;
import org.springframework.web.client.RestOperations;

import java.util.Collections;

@Service
public class RandomGenerator {

    private final VaultTemplate vaultTemplate;

    @Autowired
    public RandomGenerator(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public String generate(final Format format) {
        Data data = vaultTemplate.doWithSession(webClient -> generateRandom(webClient, format));
        return data.getRandomBytes();
    }

    private Data generateRandom(RestOperations webClient, Format format) {
        GenerateRandomResponse response = webClient.postForObject(
                "sys/tools/random", Collections.singletonMap("format", format.formatName), GenerateRandomResponse.class);
        return response.getData();
    }

    public enum Format {
        HEX("hex"), BASE64("base64");

        private final String formatName;

        Format(String formatName) {
            this.formatName = formatName;
        }
    }

    public static final class GenerateRandomResponse extends VaultResponseSupport<Data> {}

    public static final class Data {

        @JsonProperty("random_bytes")
        private String randomBytes;

        public String getRandomBytes() {
            return randomBytes;
        }
    }
}
