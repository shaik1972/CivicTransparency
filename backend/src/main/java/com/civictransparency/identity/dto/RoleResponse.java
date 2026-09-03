package com.civictransparency.identity.dto;

import java.util.UUID;

public record RoleResponse(
        UUID roleId,
        String code,
        String name) {
}