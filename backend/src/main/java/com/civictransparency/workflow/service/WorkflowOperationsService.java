package com.civictransparency.workflow.service;

import com.civictransparency.assignment.entity.ComplaintAssignment;
import com.civictransparency.assignment.repository.ComplaintAssignmentRepository;
import com.civictransparency.assignment.service.AssignmentService;
import com.civictransparency.authority.entity.AuthorityDepartment;
import com.civictransparency.authority.entity.AuthorityUser;
import com.civictransparency.authority.repository.AuthorityDepartmentRepository;
import com.civictransparency.authority.repository.AuthorityUserRepository;
import com.civictransparency.audit.service.AuditService;
import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.complaint.repository.ComplaintRepository;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.repository.UserAccountRepository;
import com.civictransparency.identity.repository.UserRoleRepository;
import com.civictransparency.notification.service.NotificationService;
import com.civictransparency.sla.entity.ComplaintSla;
import com.civictransparency.sla.service.SlaService;
import com.civictransparency.workflow.dto.AuthorityComplaintResponse;
import com.civictransparency.workflow.dto.WorkflowOperationsDtos.AssignmentResponse;
import com.civictransparency.workflow.dto.WorkflowOperationsDtos.SlaResponse;
import com.civictransparency.workflow.dto.WorkflowOperationsDtos.WorkflowResponse;
import com.civictransparency.workflow.dto.WorkflowOperationsDtos.WorkflowTransitionResponse;
import com.civictransparency.workflow.entity.ComplaintState;
import com.civictransparency.workflow.entity.Workflow;
import com.civictransparency.workflow.entity.WorkflowTransition;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class WorkflowOperationsService {

    private final ComplaintRepository complaintRepository;
    private final AuthorityDepartmentRepository departmentRepository;
    private final AuthorityUserRepository authorityUserRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final ComplaintAssignmentRepository assignmentRepository;
    private final AssignmentService assignmentService;
    private final WorkflowService workflowService;
    private final SlaService slaService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public WorkflowOperationsService(ComplaintRepository complaintRepository,
            AuthorityDepartmentRepository departmentRepository,
            AuthorityUserRepository authorityUserRepository,
            UserAccountRepository userAccountRepository,
            UserRoleRepository userRoleRepository,
            ComplaintAssignmentRepository assignmentRepository,
            AssignmentService assignmentService,
            WorkflowService workflowService,
            SlaService slaService,
            NotificationService notificationService,
            AuditService auditService) {
        this.complaintRepository = complaintRepository;
        this.departmentRepository = departmentRepository;
        this.authorityUserRepository = authorityUserRepository;
        this.userAccountRepository = userAccountRepository;
        this.userRoleRepository = userRoleRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentService = assignmentService;
        this.workflowService = workflowService;
        this.slaService = slaService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional
    public AssignmentResponse assign(UUID complaintId, UUID departmentId, String reason, UUID actorUserId) {
        Complaint complaint = requireComplaint(complaintId);
        ComplaintState currentState = workflowService.lockForUpdate(complaintId).getCurrentState();
        if (currentState == ComplaintState.RESOLVED || currentState == ComplaintState.REJECTED
                || currentState == ComplaintState.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A completed complaint cannot be reassigned");
        }
        AuthorityDepartment department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department was not found"));
        if (!"ACTIVE".equals(department.getStatus()) || !"ACTIVE".equals(department.getAuthority().getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot assign a complaint to an inactive department");
        }

        boolean systemAdmin = hasRole(actorUserId, "SYSTEM_ADMIN");
        if (!systemAdmin && !hasRole(actorUserId, "AUTHORITY_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only authority administrators can assign or reassign complaints");
        }
        AuthorityUser actorDepartment = null;
        if (!systemAdmin) {
            actorDepartment = requireAuthorityUser(actorUserId);
            if (!actorDepartment.getDepartment().getDepartmentId().equals(departmentId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Authority administrators may assign complaints only to their own department");
            }
        }

        ComplaintAssignment current = assignmentService.getCurrentAssignment(complaintId).orElse(null);
        if (current != null && current.getDepartment().getDepartmentId().equals(departmentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Complaint is already assigned to this department");
        }
        if (current != null && !systemAdmin) {
            AuthorityUser currentActorDepartment = requireAuthorityUser(actorUserId);
            if (!current.getDepartment().getDepartmentId()
                    .equals(currentActorDepartment.getDepartment().getDepartmentId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Only a system administrator can reassign a complaint across departments");
            }
        }

        UserAccount actor = userAccountRepository.findById(actorUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Actor account was not found"));
        assignmentService.assignComplaint(complaint, department, reason.trim(), actor);
        workflowService.markAssigned(complaint, reason.trim(), actorUserId);
        if (current == null) {
            notificationService.notifyComplaintRouted(complaint, department);
        } else {
            notificationService.notifyComplaintReassigned(complaint, department);
        }
        notificationService.notifyDepartmentAssigned(complaint, department);
        auditService.log(current == null ? "COMPLAINT_MANUALLY_ASSIGNED" : "COMPLAINT_REASSIGNED",
                actorUserId, complaintId,
                "Complaint " + complaint.getComplaintReference() + " assigned to " + department.getName());

        ComplaintAssignment saved = assignmentRepository.findByComplaint_ComplaintIdAndUnassignedAtIsNull(complaintId)
                .orElseThrow(() -> new IllegalStateException("The new complaint assignment was not persisted"));
        return assignmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentHistory(UUID complaintId, UUID actorUserId) {
        requireComplaint(complaintId);
        assertCanAccess(complaintId, actorUserId);
        return assignmentRepository.findByComplaint_ComplaintIdOrderByAssignedAtAsc(complaintId)
                .stream().map(WorkflowOperationsService::assignmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuthorityComplaintResponse> getAssignedComplaints(UUID actorUserId) {
        List<ComplaintAssignment> assignments;
        if (hasRole(actorUserId, "SYSTEM_ADMIN")) {
            assignments = assignmentRepository.findByUnassignedAtIsNullOrderByAssignedAtDesc();
        } else {
            if (!hasRole(actorUserId, "AUTHORITY_ADMIN") && !hasRole(actorUserId, "AUTHORITY_OFFICER")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "User is not authorized to view authority complaints");
            }
            AuthorityUser authorityUser = requireAuthorityUser(actorUserId);
            assignments = assignmentRepository
                    .findByDepartment_DepartmentIdAndUnassignedAtIsNullOrderByAssignedAtDesc(
                            authorityUser.getDepartment().getDepartmentId());
        }

        return assignments.stream().map(assignment -> {
            Complaint complaint = assignment.getComplaint();
            Workflow workflow = workflowService.getWorkflow(complaint.getComplaintId());
            return new AuthorityComplaintResponse(
                    complaint.getComplaintId(),
                    complaint.getComplaintReference(),
                    complaint.getTitle(),
                    complaint.getDescription(),
                    complaint.getCategory().getName(),
                    complaint.getSeverity(),
                    workflow.getCurrentState(),
                    complaint.getCity().getName(),
                    complaint.getCreatedAt(),
                    assignment.getDepartment().getDepartmentId(),
                    assignment.getDepartment().getName());
        }).toList();
    }

    @Transactional
    public WorkflowResponse transition(UUID complaintId, ComplaintState toState, String reason, UUID actorUserId) {
        assertCanAccess(complaintId, actorUserId);
        workflowService.transition(complaintId, toState, reason.trim(), actorUserId);
        return workflowResponse(workflowService.getWorkflow(complaintId));
    }

    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflow(UUID complaintId, UUID actorUserId) {
        assertCanAccess(complaintId, actorUserId);
        return workflowResponse(workflowService.getWorkflow(complaintId));
    }

    @Transactional(readOnly = true)
    public List<WorkflowTransitionResponse> getWorkflowHistory(UUID complaintId, UUID actorUserId) {
        assertCanAccess(complaintId, actorUserId);
        return workflowService.getHistory(complaintId).stream()
                .map(WorkflowOperationsService::transitionResponse).toList();
    }

    @Transactional
    public SlaResponse pauseSla(UUID complaintId, String reason, UUID actorUserId) {
        assertCanAccess(complaintId, actorUserId);
        slaService.pause(complaintId, reason.trim());
        auditService.log("COMPLAINT_SLA_PAUSED", actorUserId, complaintId,
                "SLA paused for complaint " + requireComplaint(complaintId).getComplaintReference());
        return getSlaResponse(complaintId);
    }

    @Transactional
    public SlaResponse resumeSla(UUID complaintId, UUID actorUserId) {
        assertCanAccess(complaintId, actorUserId);
        slaService.resume(complaintId);
        auditService.log("COMPLAINT_SLA_RESUMED", actorUserId, complaintId,
                "SLA resumed for complaint " + requireComplaint(complaintId).getComplaintReference());
        return getSlaResponse(complaintId);
    }

    @Transactional(readOnly = true)
    public SlaResponse getSla(UUID complaintId, UUID actorUserId) {
        assertCanAccess(complaintId, actorUserId);
        return getSlaResponse(complaintId);
    }

    private SlaResponse getSlaResponse(UUID complaintId) {
        ComplaintSla sla = slaService.findForComplaint(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No SLA exists for this complaint"));
        return new SlaResponse(sla.getSlaId(), sla.getPolicy().getPolicyId(), sla.getPolicy().getTargetHours(),
                sla.getDeadline(), sla.getIsPaused(), sla.getPausedAt(), sla.getPausedReason(),
                sla.getResolvedAt(), sla.getStatus());
    }

    private void assertCanAccess(UUID complaintId, UUID actorUserId) {
        if (hasRole(actorUserId, "SYSTEM_ADMIN")) {
            return;
        }
        if (!hasRole(actorUserId, "AUTHORITY_ADMIN") && !hasRole(actorUserId, "AUTHORITY_OFFICER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authorized for authority operations");
        }
        ComplaintAssignment assignment = assignmentService.getCurrentAssignment(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Complaint has no current authority assignment"));
        AuthorityUser authorityUser = requireAuthorityUser(actorUserId);
        if (!"ACTIVE".equals(authorityUser.getDepartment().getStatus())
                || !"ACTIVE".equals(authorityUser.getDepartment().getAuthority().getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authority department is inactive");
        }
        if (!authorityUser.getDepartment().getDepartmentId().equals(assignment.getDepartment().getDepartmentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Complaint is assigned to a different authority department");
        }
    }

    private boolean hasRole(UUID userId, String roleCode) {
        return userRoleRepository.findByIdUserId(userId).stream()
                .anyMatch(userRole -> roleCode.equals(userRole.getRole().getCode()));
    }

    private AuthorityUser requireAuthorityUser(UUID userId) {
        AuthorityUser authorityUser = authorityUserRepository.findByUserAccount_UserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "User is not assigned to an authority department"));
        if (!"ACTIVE".equals(authorityUser.getDepartment().getStatus())
                || !"ACTIVE".equals(authorityUser.getDepartment().getAuthority().getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authority department is inactive");
        }
        return authorityUser;
    }

    private Complaint requireComplaint(UUID complaintId) {
        return complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint was not found"));
    }

    private static AssignmentResponse assignmentResponse(ComplaintAssignment assignment) {
        return new AssignmentResponse(assignment.getAssignmentId(),
                assignment.getComplaint().getComplaintId(),
                assignment.getDepartment().getDepartmentId(),
                assignment.getDepartment().getName(),
                assignment.getAssignedAt(),
                assignment.getUnassignedAt(),
                assignment.getAssignmentReason(),
                assignment.getAssignedBy() == null ? null : assignment.getAssignedBy().getUserId());
    }

    private static WorkflowResponse workflowResponse(Workflow workflow) {
        return new WorkflowResponse(workflow.getComplaint().getComplaintId(), workflow.getCurrentState(),
                workflow.getStartedAt(), workflow.getCompletedAt(), workflow.getVersion());
    }

    private static WorkflowTransitionResponse transitionResponse(WorkflowTransition transition) {
        return new WorkflowTransitionResponse(transition.getTransitionId(), transition.getFromState(),
                transition.getToState(), transition.getReason(),
                transition.getActor() == null ? null : transition.getActor().getUserId(), transition.getCreatedAt());
    }
}
