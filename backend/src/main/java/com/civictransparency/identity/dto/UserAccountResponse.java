package com.civictransparency.identity.dto;

import java.util.UUID;

public record UserAccountResponse(
        UUID userId,
        String username,
        String email,
        String status) {
}