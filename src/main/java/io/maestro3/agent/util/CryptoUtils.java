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

package io.maestro3.agent.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.List;

public class CryptoUtils {
    public static final int GCM_IV_LENGTH = 16;
    public static final String SECRET_ALG = "PBKDF2WithHmacSHA256";
    // Valid AES key sizes in bytes.
    // NOTE: The values need to be listed in an *increasing* order
    private static final List<Integer> AES_KEYSIZES = Arrays.asList(16, 24, 32);
    private static final SecureRandom secureRandom = new SecureRandom();

    private CryptoUtils() {
        throw new RuntimeException("Instantiation forbidden");
    }


    /**
     * Encrypt a plaintext with given key.
     *
     * @param plaintext to encrypt (utf-8 encoding will be used)
     * @param secret    the secret key used for encryption to encrypt
     * @return encrypted message with IV of length {@link #GCM_IV_LENGTH}
     * @throws IllegalArgumentException if the combination of arguments is invalid
     */
    public static String encrypt(String plaintext, String secret) {
        validateData("plainText data", plaintext);
        byte[] dataToEncrypt = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encrypt(dataToEncrypt, secret);
        return Base64.encodeBase64String(encrypted).trim();
    }

    public static byte[] encrypt(byte[] bytesToEncrypt, String secret) {
        validateDataBytes("bytes to encrypt", bytesToEncrypt);
        validateSecret(secret);
        byte[] iv = new byte[GCM_IV_LENGTH]; //NEVER REUSE THIS IV WITH SAME KEY
        secureRandom.nextBytes(iv);

        byte[] cipherText;
        try {
            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secret, iv);
            cipherText = cipher.doFinal(bytesToEncrypt);
        } catch (NoSuchPaddingException | BadPaddingException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new IllegalArgumentException("Cannot encrypt: " + e.getMessage(), e);
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);

        return byteBuffer.array();
    }

    /**
     * Decrypts encrypted message (see {@link #encrypt(String, String)}).
     *
     * @param encrypted iv with encrypted data
     * @param secret    used to decrypt
     * @return original plaintext
     * @throws IllegalArgumentException if the combination of arguments is invalid
     */
    public static String decrypt(String encrypted, String secret) {
        validateData("encrypted data", encrypted);
        validateSecret(secret);
        byte[] cipherMessage = Base64.decodeBase64(encrypted.getBytes());
        byte[] decryptedBytes = decrypt(cipherMessage, secret);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static byte[] decrypt(byte[] encryptedBytesWithIv, String secret) {
        validateDataBytes("encryptedBytesWithIv", encryptedBytesWithIv);
        validateSecret(secret);
        try {
            final Cipher cipher = initCipher(Cipher.DECRYPT_MODE, secret, encryptedBytesWithIv);
            //use everything from GCM_IV_LENGTH bytes on as ciphertext
            return cipher.doFinal(encryptedBytesWithIv, GCM_IV_LENGTH, encryptedBytesWithIv.length - GCM_IV_LENGTH);
        } catch (NoSuchPaddingException | BadPaddingException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new IllegalArgumentException("Cannot decrypt: " + e.getMessage(), e);
        }
    }

    private static Cipher initCipher(int cipherMode, String secret, byte[] cipherMessageWithIV) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] secretBytes = Base64.decodeBase64(secret);
        Key secretKey = new SecretKeySpec(secretBytes, "AES");
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        //use first GCM_IV_LENGTH bytes for iv
        AlgorithmParameterSpec gcmIv = new GCMParameterSpec(128, cipherMessageWithIV, 0, GCM_IV_LENGTH);
        cipher.init(cipherMode, secretKey, gcmIv);
        /*if (associatedData != null) {
            cipher.updateAAD(associatedData);
        }*/
        return cipher;
    }

    private static void validateData(String field, String data) {
        if (data == null) {
            throw new IllegalArgumentException(String.format("%s cannot be null", field));
        }
    }

    private static void validateDataBytes(String field, byte[] dataBytes) {
        if (dataBytes == null) {
            throw new IllegalArgumentException(String.format("%s cannot be null", field));
        }
    }

    private static void validateSecret(String secret) {
        if (secret == null) {
            throw new IllegalArgumentException("Secret cannot be null");
        }
        int len = secret.getBytes(StandardCharsets.UTF_8).length;
        // check if the specified length (in bytes) is a valid keysize for AES
        boolean valid = false;
        for (int aesKeysize : AES_KEYSIZES) {
            if (len == aesKeysize) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new IllegalArgumentException(String.format("Invalid secret format: %s bytes. Should be of size %s bytes", len, AES_KEYSIZES));
        }
    }

}
