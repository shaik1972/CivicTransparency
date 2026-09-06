package com.civictransparency.sla.repository;

import com.civictransparency.sla.entity.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, UUID> {

    @Query("SELECT p FROM SlaPolicy p WHERE p.isActive = true " +
           "AND p.category.categoryId = :categoryId " +
           "AND (p.subcategory IS NULL OR p.subcategory.subcategoryId = :subcategoryId) " +
           "AND p.severity = :severity " +
           "ORDER BY p.subcategory.subcategoryId NULLS LAST")
    Optional<SlaPolicy> findBestMatch(UUID categoryId, UUID subcategoryId, String severity);
}
