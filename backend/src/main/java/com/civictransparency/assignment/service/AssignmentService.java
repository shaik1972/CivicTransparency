package com.civictransparency.assignment.service;

import com.civictransparency.assignment.entity.ComplaintAssignment;
import com.civictransparency.assignment.repository.ComplaintAssignmentRepository;
import com.civictransparency.authority.entity.AuthorityDepartment;
import com.civictransparency.complaint.entity.Complaint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AssignmentService {
    
    private final ComplaintAssignmentRepository assignmentRepository;

    public AssignmentService(ComplaintAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public void assignComplaint(Complaint complaint, AuthorityDepartment department) {
        // Mark previous assignment as unassigned if exists
        assignmentRepository.findByComplaint_ComplaintIdAndUnassignedAtIsNull(complaint.getComplaintId())
            .ifPresent(ComplaintAssignment::markUnassigned);

        // Create new assignment
        ComplaintAssignment newAssignment = new ComplaintAssignment(
            UUID.randomUUID(),
            complaint,
            department,
            "AUTOMATED_ROUTING",
            null
        );
        assignmentRepository.save(newAssignment);
    }
}
