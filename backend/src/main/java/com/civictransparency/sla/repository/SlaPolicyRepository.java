package com.civictransparency.sla.repository;

import com.civictransparency.sla.entity.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, UUID> {

    @Query("SELECT p FROM SlaPolicy p WHERE p.isActive = true " +
            "AND p.category.categoryId = :categoryId " +
            "AND (p.subcategory IS NULL OR p.subcategory.subcategoryId = :subcategoryId) " +
            "AND p.severity = :severity " +
            "ORDER BY CASE WHEN p.subcategory IS NULL THEN 0 ELSE 1 END DESC, p.policyId ASC")
    List<SlaPolicy> findBestMatches(UUID categoryId, UUID subcategoryId, String severity);

    boolean existsByCategory_CategoryIdAndSeverityAndIsActiveTrueAndSubcategory_SubcategoryId(
            UUID categoryId, String severity, UUID subcategoryId);

    boolean existsByCategory_CategoryIdAndSeverityAndIsActiveTrueAndSubcategoryIsNull(
            UUID categoryId, String severity);
}
