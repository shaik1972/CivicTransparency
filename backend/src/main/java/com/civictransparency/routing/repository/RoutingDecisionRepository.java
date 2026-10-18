package com.civictransparency.routing.repository;

import com.civictransparency.routing.entity.RoutingDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface RoutingDecisionRepository extends JpaRepository<RoutingDecision, UUID> {
    Optional<RoutingDecision> findByComplaint_ComplaintId(UUID complaintId);
}
