package com.civictransparency.geography.dto;

import java.util.UUID;

public record CityResponse(
        UUID cityId,
        UUID stateId,
        String code,
        String name) {
}
