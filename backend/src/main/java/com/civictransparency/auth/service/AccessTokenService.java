package com.civictransparency.auth.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AccessTokenService {

    String issue(UUID userId, List<String> roles, OffsetDateTime expiresAt);

    default String issueRefresh(UUID userId, List<String> roles, OffsetDateTime expiresAt) {
        return issue(userId, roles, expiresAt);
    }

    UUID verify(String token);

    default UUID verifyRefresh(String token) {
        return verify(token);
    }

    default List<String> getRoles(String token) {
        return List.of();
    }
}
