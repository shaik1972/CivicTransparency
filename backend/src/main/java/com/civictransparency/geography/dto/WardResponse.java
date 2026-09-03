package com.civictransparency.geography.dto;

import java.util.UUID;

public record WardResponse(
        UUID wardId,
        UUID cityId,
        String code,
        String name) {
}
