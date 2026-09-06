package com.civictransparency.routing.repository;

import com.civictransparency.routing.entity.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RoutingRuleRepository extends JpaRepository<RoutingRule, UUID> {
    
    @Query("SELECT r FROM RoutingRule r WHERE r.isActive = true " +
           "AND (r.state IS NULL OR r.state.stateId = :stateId) " +
           "AND (r.city IS NULL OR r.city.cityId = :cityId) " +
           "AND (r.ward IS NULL OR r.ward.wardId = :wardId) " +
           "AND (r.category IS NULL OR r.category.categoryId = :categoryId) " +
           "AND (r.subcategory IS NULL OR r.subcategory.subcategoryId = :subcategoryId) " +
           "AND (r.effectiveFrom IS NULL OR r.effectiveFrom <= CURRENT_TIMESTAMP) " +
           "AND (r.effectiveTo IS NULL OR r.effectiveTo >= CURRENT_TIMESTAMP) " +
           "ORDER BY r.priority DESC")
    List<RoutingRule> findMatchingActiveRules(UUID stateId, UUID cityId, UUID wardId, UUID categoryId, UUID subcategoryId);
}
