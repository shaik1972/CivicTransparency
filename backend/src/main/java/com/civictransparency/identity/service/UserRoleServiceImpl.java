package com.civictransparency.identity.service;

import com.civictransparency.identity.dto.UserRoleResponse;
import com.civictransparency.identity.entity.Role;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.entity.UserRole;
import com.civictransparency.identity.repository.RoleRepository;
import com.civictransparency.identity.repository.UserAccountRepository;
import com.civictransparency.identity.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserRoleServiceImpl implements UserRoleService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public UserRoleServiceImpl(
            UserAccountRepository userAccountRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository) {

        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public UserRoleResponse assignRole(
            UUID userId,
            UUID roleId) {

        if (userRoleRepository
                .existsByIdUserIdAndIdRoleId(userId, roleId)) {

            throw new IllegalArgumentException(
                    "Role is already assigned to user");
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User account not found: "
                                + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Role not found: "
                                + roleId));

        UserRole userRole = new UserRole(user, role);

        UserRole saved = userRoleRepository.save(userRole);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponse> getRolesForUser(
            UUID userId) {

        return userRoleRepository
                .findByIdUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponse> getUsersForRole(
            UUID roleId) {

        return userRoleRepository
                .findByIdRoleId(roleId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(
            UUID userId,
            UUID roleId) {

        return userRoleRepository
                .existsByIdUserIdAndIdRoleId(
                        userId,
                        roleId);
    }

    @Override
    public void removeRole(
            UUID userId,
            UUID roleId) {

        UserRole userRole = userRoleRepository
                .findById(
                        new com.civictransparency.identity.entity.UserRoleId(
                                userId,
                                roleId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "User role assignment not found"));

        userRoleRepository.delete(userRole);
    }

    private UserRoleResponse toResponse(
            UserRole userRole) {

        return new UserRoleResponse(
                userRole.getUser().getUserId(),
                userRole.getRole().getRoleId(),
                userRole.getRole().getCode(),
                userRole.getRole().getName());
    }
}
