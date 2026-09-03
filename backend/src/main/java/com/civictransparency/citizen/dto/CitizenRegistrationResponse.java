package com.civictransparency.citizen.dto;

import java.util.UUID;

public record CitizenRegistrationResponse(

        UUID citizenId,

        UUID userId,

        String username,

        String publicAlias,

        String profileStatus,

        String userStatus,

        String roleCode) {
}
