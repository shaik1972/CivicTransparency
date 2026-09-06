package com.civictransparency.authority.repository;

import com.civictransparency.authority.entity.AuthorityDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AuthorityDepartmentRepository extends JpaRepository<AuthorityDepartment, UUID> {
}
