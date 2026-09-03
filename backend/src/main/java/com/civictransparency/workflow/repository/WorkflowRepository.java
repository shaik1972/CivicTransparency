package com.civictransparency.workflow.repository;

import com.civictransparency.workflow.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    Optional<Workflow> findByComplaintComplaintId(UUID complaintId);
}
