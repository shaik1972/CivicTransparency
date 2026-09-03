package com.civictransparency.geography.dto;

import java.util.UUID;

public record StateResponse(
        UUID stateId,
        String code,
        String name) {
}
