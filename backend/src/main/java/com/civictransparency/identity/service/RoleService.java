package com.civictransparency.identity.service;

import com.civictransparency.identity.dto.CreateRoleRequest;
import com.civictransparency.identity.dto.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse getRole(UUID roleId);

    RoleResponse getRoleByCode(String code);

    List<RoleResponse> getAllRoles();
}