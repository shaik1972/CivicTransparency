package com.civictransparency.auth.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AccessTokenService {

    String issue(UUID userId, List<String> roles, OffsetDateTime expiresAt);
}
