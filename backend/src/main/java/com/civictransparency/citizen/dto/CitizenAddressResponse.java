package com.civictransparency.citizen.dto;

import java.util.UUID;

public record CitizenAddressResponse(
        UUID addressId,
        UUID citizenId,
        UUID cityId,
        String postalCode,
        boolean primary) {
}
