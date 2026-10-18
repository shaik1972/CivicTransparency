package com.civictransparency.routing.repository;

import com.civictransparency.complaint.entity.Severity;
import com.civictransparency.routing.entity.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface RoutingRuleRepository extends JpaRepository<RoutingRule, UUID> {

    @Query("SELECT r FROM RoutingRule r WHERE r.isActive = true " +
            "AND r.department.status = 'ACTIVE' " +
            "AND r.department.authority.status = 'ACTIVE' " +
            "AND (r.department.authority.state IS NULL OR r.department.authority.state.stateId = :stateId) " +
            "AND (r.department.authority.city IS NULL OR r.department.authority.city.cityId = :cityId) " +
            "AND (r.state IS NULL OR r.state.stateId = :stateId) " +
            "AND (r.city IS NULL OR r.city.cityId = :cityId) " +
            "AND (r.ward IS NULL OR r.ward.wardId = :wardId) " +
            "AND (r.category IS NULL OR r.category.categoryId = :categoryId) " +
            "AND (r.subcategory IS NULL OR r.subcategory.subcategoryId = :subcategoryId) " +
            "AND (r.severity IS NULL OR r.severity = :severity) " +
            "AND (r.effectiveFrom IS NULL OR r.effectiveFrom <= CURRENT_TIMESTAMP) " +
            "AND (r.effectiveTo IS NULL OR r.effectiveTo >= CURRENT_TIMESTAMP) " +
            "ORDER BY r.priority DESC")
    List<RoutingRule> findMatchingActiveRules(UUID stateId, UUID cityId, UUID wardId, UUID categoryId,
            UUID subcategoryId, Severity severity);
}
