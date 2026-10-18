package com.civictransparency.authority.repository;

import com.civictransparency.authority.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, UUID> {
    boolean existsByCode(String code);
}
