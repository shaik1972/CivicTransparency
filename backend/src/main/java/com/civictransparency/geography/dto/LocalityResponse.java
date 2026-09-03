package com.civictransparency.geography.dto;

import java.util.UUID;

public record LocalityResponse(
        UUID localityId,
        UUID wardId,
        String name) {
}
