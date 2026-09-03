package com.civictransparency.identity.repository;

import com.civictransparency.identity.entity.UserRole;
import com.civictransparency.identity.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByIdUserId(UUID userId);

    List<UserRole> findByIdRoleId(UUID roleId);

    boolean existsByIdUserIdAndIdRoleId(UUID userId, UUID roleId);
}