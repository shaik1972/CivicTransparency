package com.civictransparency.citizen.dto;

import java.util.UUID;

public record CitizenProfileResponse(
        UUID citizenId,
        UUID userId,
        String publicAlias,
        String profileStatus) {
}
