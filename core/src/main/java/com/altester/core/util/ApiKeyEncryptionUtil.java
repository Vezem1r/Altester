package com.altester.core.util;

import com.altester.core.config.AppConfig;
import com.altester.core.exception.ApiKeyException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiKeyEncryptionUtil {
  private final AppConfig appConfig;

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final String KEY_ALGORITHM = "AES";
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;
  private static final String ERROR_RESPONSE = "API key cannot be null or empty";

  /**
   * Encrypts an API key
   *
   * @param apiKey The API key to encrypt
   * @return The encrypted API key
   * @throws ApiKeyException If encryption fails or inputs are invalid
   */
  public String encrypt(String apiKey) {
    if (apiKey == null || apiKey.isEmpty()) {
      throw ApiKeyException.invalidInputKey(ERROR_RESPONSE);
    }

    try {
      byte[] decodedKey = Base64.getDecoder().decode(appConfig.getSecretKey());

      if (decodedKey.length != 16 && decodedKey.length != 24 && decodedKey.length != 32) {
        throw ApiKeyException.invalidKeyLength();
      }

      byte[] iv = new byte[GCM_IV_LENGTH];
      new SecureRandom().nextBytes(iv);

      GCMParameterSpec gcmParamSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

      Key key = new SecretKeySpec(decodedKey, KEY_ALGORITHM);
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, key, gcmParamSpec);

      byte[] encryptedBytes = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));

      byte[] combined = new byte[iv.length + encryptedBytes.length];
      System.arraycopy(iv, 0, combined, 0, iv.length);
      System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

      return Base64.getEncoder().encodeToString(combined);
    } catch (ApiKeyException e) {
      throw e;
    } catch (Exception e) {
      throw ApiKeyException.encryptionError("Error encrypting API key: " + e.getMessage());
    }
  }

  /**
   * Decrypts an encrypted API key
   *
   * @param encryptedKey The encrypted API key to decrypt
   * @return The decrypted API key
   * @throws ApiKeyException If decryption fails or inputs are invalid
   */
  public String decrypt(String encryptedKey) {
    if (encryptedKey == null || encryptedKey.isEmpty()) {
      throw ApiKeyException.invalidInputKey("Encrypted API key cannot be null or empty");
    }

    try {
      byte[] decodedKey = Base64.getDecoder().decode(appConfig.getSecretKey());

      if (decodedKey.length != 16 && decodedKey.length != 24 && decodedKey.length != 32) {
        throw ApiKeyException.invalidKeyLength();
      }

      byte[] combined = Base64.getDecoder().decode(encryptedKey);

      byte[] iv = new byte[GCM_IV_LENGTH];
      System.arraycopy(combined, 0, iv, 0, iv.length);

      GCMParameterSpec gcmParamSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

      byte[] cipherText = new byte[combined.length - iv.length];
      System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

      Key key = new SecretKeySpec(decodedKey, KEY_ALGORITHM);
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, key, gcmParamSpec);

      byte[] decryptedBytes = cipher.doFinal(cipherText);

      return new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      throw ApiKeyException.decryptionError("Invalid Base64 encoded string: " + e.getMessage());
    } catch (ApiKeyException e) {
      throw e;
    } catch (Exception e) {
      throw ApiKeyException.decryptionError("Error decrypting API key: " + e.getMessage());
    }
  }

  /**
   * Extracts the first N characters from an API key
   *
   * @param apiKey The API key
   * @param count Number of characters to extract
   * @return The prefix of the API key
   * @throws ApiKeyException If the API key is invalid
   */
  public String extractPrefix(String apiKey, int count) throws ApiKeyException {
    if (apiKey == null || apiKey.isEmpty()) {
      throw ApiKeyException.invalidInputKey(ERROR_RESPONSE);
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
   *
   * @param apiKey The API key
   * @param count Number of characters to extract
   * @return The suffix of the API key
   * @throws ApiKeyException If the API key is invalid
   */
  public String extractSuffix(String apiKey, int count) throws ApiKeyException {
    if (apiKey == null || apiKey.isEmpty()) {
      throw ApiKeyException.invalidInputKey(ERROR_RESPONSE);
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
