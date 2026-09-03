package com.civictransparency.security;

public interface PasswordHashingService {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String storedHash);
}
