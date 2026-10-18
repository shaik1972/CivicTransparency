package com.civictransparency.workflow.repository;

import com.civictransparency.workflow.entity.Workflow;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.util.Optional;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    Optional<Workflow> findByComplaintComplaintId(UUID complaintId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Workflow w WHERE w.complaint.complaintId = :complaintId")
    Optional<Workflow> findByComplaintIdForUpdate(@Param("complaintId") UUID complaintId);
}
