package com.civictransparency.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PiiEncryptionServiceImpl
        implements PiiEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private static final int GCM_TAG_LENGTH = 128;

    private static final int IV_LENGTH = 12;

    private final SecretKeySpec secretKey;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public PiiEncryptionServiceImpl(
            @Value("${civic.security.pii-encryption-key}")
            String encodedKey) {

        byte[] key;

        try {
            key = Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "CIVIC_ENCRYPTION_KEY must be Base64 encoded",
                    ex);
        }

        if (key.length != 32) {
            throw new IllegalStateException(
                    "CIVIC_ENCRYPTION_KEY must decode to 32 bytes");
        }

        this.secretKey = new SecretKeySpec(key, "AES");
    }

    @Override
    public String encrypt(String plaintext) {

        if (plaintext == null) {
            throw new IllegalArgumentException(
                    "Plaintext cannot be null");
        }

        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH,
                            iv));

            byte[] encrypted = cipher.doFinal(
                    plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined =
                    new byte[iv.length + encrypted.length];

            System.arraycopy(
                    iv,
                    0,
                    combined,
                    0,
                    iv.length);

            System.arraycopy(
                    encrypted,
                    0,
                    combined,
                    iv.length,
                    encrypted.length);

            return Base64.getEncoder()
                    .encodeToString(combined);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to encrypt PII",
                    ex);
        }
    }

    @Override
    public String decrypt(String ciphertext) {

        if (ciphertext == null) {
            throw new IllegalArgumentException(
                    "Ciphertext cannot be null");
        }

        try {
            byte[] combined =
                    Base64.getDecoder().decode(ciphertext);

            if (combined.length <= IV_LENGTH) {
                throw new IllegalArgumentException(
                        "Invalid encrypted value");
            }

            byte[] iv = new byte[IV_LENGTH];

            byte[] encrypted =
                    new byte[combined.length - IV_LENGTH];

            System.arraycopy(
                    combined,
                    0,
                    iv,
                    0,
                    IV_LENGTH);

            System.arraycopy(
                    combined,
                    IV_LENGTH,
                    encrypted,
                    0,
                    encrypted.length);

            Cipher cipher =
                    Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH,
                            iv));

            byte[] decrypted =
                    cipher.doFinal(encrypted);

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to decrypt PII",
                    ex);
        }
    }
}
