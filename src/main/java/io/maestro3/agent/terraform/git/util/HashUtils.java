package io.maestro3.agent.terraform.git.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {

    public static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    private HashUtils() {
        throw new UnsupportedOperationException("Class is not designed for an instantiation");
    }

    public static byte[] hmac(final String algorithm, final String secret, final String payload)
            throws NoSuchAlgorithmException, InvalidKeyException {
        final SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
        final Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);
        return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
    }

    public static String hmacHex(final String algorithm, final String secret, final String payload) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] hmac = hmac(algorithm, secret, payload);
        return new String(hmac);
    }
}
