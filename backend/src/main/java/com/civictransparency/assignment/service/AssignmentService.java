package com.civictransparency.assignment.service;

import com.civictransparency.assignment.entity.ComplaintAssignment;
import com.civictransparency.assignment.repository.ComplaintAssignmentRepository;
import com.civictransparency.authority.entity.AuthorityDepartment;
import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.identity.entity.UserAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AssignmentService {
    
    private final ComplaintAssignmentRepository assignmentRepository;

    public AssignmentService(ComplaintAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public void assignComplaint(Complaint complaint, AuthorityDepartment department) {
        assignComplaint(complaint, department, "AUTOMATED_ROUTING", null);
    }

    @Transactional
    public void assignComplaint(Complaint complaint, AuthorityDepartment department, String reason,
            UserAccount assignedBy) {
        assignmentRepository.findByComplaint_ComplaintIdAndUnassignedAtIsNull(complaint.getComplaintId())
                .ifPresent(assignment -> {
                    assignment.markUnassigned();
                    assignmentRepository.save(assignment);
                });

        ComplaintAssignment newAssignment = new ComplaintAssignment(
            UUID.randomUUID(),
            complaint,
            department,
            reason,
            assignedBy
        );
        assignmentRepository.save(newAssignment);
    }

    @Transactional(readOnly = true)
    public Optional<ComplaintAssignment> getCurrentAssignment(UUID complaintId) {
        return assignmentRepository.findByComplaint_ComplaintIdAndUnassignedAtIsNull(complaintId);
    }
}
