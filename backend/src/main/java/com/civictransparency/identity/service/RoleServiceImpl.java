package com.civictransparency.identity.service;

import com.civictransparency.identity.dto.CreateRoleRequest;
import com.civictransparency.identity.dto.RoleResponse;
import com.civictransparency.identity.entity.Role;
import com.civictransparency.identity.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {

        if (roleRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException(
                    "Role code already exists: "
                            + request.code());
        }

        Role role = new Role(
                request.code(),
                request.name());

        Role saved = roleRepository.save(role);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRole(UUID roleId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Role not found: "
                                + roleId));

        return toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleByCode(String code) {

        Role role = roleRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Role not found: "
                                + code));

        return toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RoleResponse toResponse(Role role) {

        return new RoleResponse(
                role.getRoleId(),
                role.getCode(),
                role.getName());
    }
}