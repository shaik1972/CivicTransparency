package com.civictransparency.assignment.repository;

import com.civictransparency.assignment.entity.ComplaintAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ComplaintAssignmentRepository extends JpaRepository<ComplaintAssignment, UUID> {
    Optional<ComplaintAssignment> findByComplaint_ComplaintIdAndUnassignedAtIsNull(UUID complaintId);
}
