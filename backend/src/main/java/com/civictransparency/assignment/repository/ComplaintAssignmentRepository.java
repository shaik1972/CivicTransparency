package com.civictransparency.assignment.repository;

import com.civictransparency.assignment.entity.ComplaintAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface ComplaintAssignmentRepository extends JpaRepository<ComplaintAssignment, UUID> {
    Optional<ComplaintAssignment> findByComplaint_ComplaintIdAndUnassignedAtIsNull(UUID complaintId);

    List<ComplaintAssignment> findByComplaint_ComplaintIdOrderByAssignedAtAsc(UUID complaintId);

    List<ComplaintAssignment> findByDepartment_DepartmentIdAndUnassignedAtIsNullOrderByAssignedAtDesc(
            UUID departmentId);

    List<ComplaintAssignment> findByUnassignedAtIsNullOrderByAssignedAtDesc();
}
