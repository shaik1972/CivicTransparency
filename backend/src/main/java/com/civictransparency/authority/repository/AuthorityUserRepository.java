package com.civictransparency.authority.repository;

import com.civictransparency.authority.entity.AuthorityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorityUserRepository extends JpaRepository<AuthorityUser, UUID> {
    List<AuthorityUser> findAllByDepartment_DepartmentId(UUID departmentId);

    Optional<AuthorityUser> findByUserAccount_UserId(UUID userId);
}
