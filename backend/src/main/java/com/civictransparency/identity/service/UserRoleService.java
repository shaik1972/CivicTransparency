package com.civictransparency.identity.service;

import com.civictransparency.identity.dto.UserRoleResponse;

import java.util.List;
import java.util.UUID;

public interface UserRoleService {

    UserRoleResponse assignRole(
            UUID userId,
            UUID roleId);

    List<UserRoleResponse> getRolesForUser(UUID userId);

    List<UserRoleResponse> getUsersForRole(UUID roleId);

    boolean hasRole(UUID userId, UUID roleId);

    void removeRole(UUID userId, UUID roleId);
}
