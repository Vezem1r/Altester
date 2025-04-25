package com.altester.core.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

@Component
public class ApiKeyEncryptionUtil {

    @Value("${api-key.encryption.secret}")
    private String secret;

    private static final String ALGORITHM = "AES";

    /**
     * Encrypts an API key
     * @param apiKey The API key to encrypt
     * @return The encrypted API key
     */
    public String encrypt(String apiKey) {
        try {
            Key key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting API key", e);
        }
    }

    /**
     * Extracts the first N characters from an API key
     * @param apiKey The API key
     * @param count Number of characters to extract
     * @return The prefix of the API key
     */
    public String extractPrefix(String apiKey, int count) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.length() <= count) {
            return apiKey;
        }
        return apiKey.substring(0, count);
    }

    /**
     * Extracts the last N characters from an API key
     * @param apiKey The API key
     * @param count Number of characters to extract
     * @return The suffix of the API key
     */
    public String extractSuffix(String apiKey, int count) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.length() <= count) {
            return apiKey;
        }
        return apiKey.substring(apiKey.length() - count);
    }
}
