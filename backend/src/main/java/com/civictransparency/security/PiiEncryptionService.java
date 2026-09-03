package com.civictransparency.security;

public interface PiiEncryptionService {

    String encrypt(String plaintext);

    String decrypt(String ciphertext);
}
