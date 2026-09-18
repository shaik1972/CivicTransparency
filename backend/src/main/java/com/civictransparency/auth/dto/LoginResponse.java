package com.civictransparency.auth.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record LoginResponse(
                String accessToken,
                String refreshToken,
                String tokenType,
                OffsetDateTime expiresAt,
                UUID userId,
                UUID citizenId,
                String username,
                String publicAlias,
                List<String> roles) {
}
