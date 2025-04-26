package com.altester.core.util;

import com.altester.core.exception.ApiKeyException;
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
     * @throws ApiKeyException If encryption fails or inputs are invalid
     */
    public String encrypt(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw ApiKeyException.invalidInputKey("API key cannot be null or empty");
        }

        try {
            byte[] decodedKey = Base64.getDecoder().decode(secret);

            if (decodedKey.length != 16 && decodedKey.length != 24 && decodedKey.length != 32) {
                throw ApiKeyException.invalidKeyLength();
            }

            Key key = new SecretKeySpec(decodedKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (ApiKeyException e) {
            throw e;
        } catch (Exception e) {
            throw ApiKeyException.encryptionError("Error encrypting API key: " + e.getMessage());
        }
    }

    /**
     * Extracts the first N characters from an API key
     * @param apiKey The API key
     * @param count Number of characters to extract
     * @return The prefix of the API key
     * @throws ApiKeyException If the API key is invalid
     */
    public String extractPrefix(String apiKey, int count) throws ApiKeyException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw ApiKeyException.invalidInputKey("API key cannot be null or empty");
        }

        if (count < 0) {
            throw ApiKeyException.invalidInputKey("Count cannot be negative");
        }

        if (apiKey.length() <= count) {
            return apiKey;
        }
        return apiKey.substring(0, count);
    }

    /**
     * Extracts the last N characters from an API key
     * @param apiKey The API key
     * @param count Number of characters to extract
     * @return The suffix of the API key
     * @throws ApiKeyException If the API key is invalid
     */
    public String extractSuffix(String apiKey, int count) throws ApiKeyException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw ApiKeyException.invalidInputKey("API key cannot be null or empty");
        }

        if (count < 0) {
            throw ApiKeyException.invalidInputKey("Count cannot be negative");
        }

        if (apiKey.length() <= count) {
            return apiKey;
        }
        return apiKey.substring(apiKey.length() - count);
    }
}
