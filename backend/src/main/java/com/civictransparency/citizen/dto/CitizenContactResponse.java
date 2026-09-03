package com.civictransparency.citizen.dto;

import java.util.UUID;

public record CitizenContactResponse(
        UUID citizenId,
        boolean verifiedPhone,
        boolean verifiedEmail) {
}
