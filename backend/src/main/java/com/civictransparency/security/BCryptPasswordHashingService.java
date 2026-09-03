package com.civictransparency.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BCryptPasswordHashingService
        implements PasswordHashingService {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHashingService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(
            String rawPassword,
            String storedHash) {

        return passwordEncoder.matches(
                rawPassword,
                storedHash);
    }
}
