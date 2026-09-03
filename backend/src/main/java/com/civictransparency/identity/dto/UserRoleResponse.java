package com.civictransparency.identity.dto;

import java.util.UUID;

public record UserRoleResponse(
        UUID userId,
        UUID roleId,
        String roleCode,
        String roleName) {
}
