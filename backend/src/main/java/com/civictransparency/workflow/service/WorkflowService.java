package com.civictransparency.workflow.service;

import com.civictransparency.audit.service.AuditService;
import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.repository.UserAccountRepository;
import com.civictransparency.sla.service.SlaService;
import com.civictransparency.workflow.entity.ComplaintState;
import com.civictransparency.workflow.entity.Workflow;
import com.civictransparency.workflow.entity.WorkflowTransition;
import com.civictransparency.workflow.repository.WorkflowRepository;
import com.civictransparency.workflow.repository.WorkflowTransitionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowService {

    private static final Map<ComplaintState, EnumSet<ComplaintState>> ALLOWED_TRANSITIONS = Map.of(
            ComplaintState.SUBMITTED, EnumSet.of(ComplaintState.ROUTED, ComplaintState.ASSIGNED, ComplaintState.REJECTED),
            ComplaintState.ROUTED, EnumSet.of(ComplaintState.ASSIGNED, ComplaintState.REJECTED),
            ComplaintState.ASSIGNED, EnumSet.of(ComplaintState.IN_PROGRESS, ComplaintState.ON_HOLD,
                    ComplaintState.RESOLVED, ComplaintState.REJECTED),
            ComplaintState.IN_PROGRESS, EnumSet.of(ComplaintState.ON_HOLD, ComplaintState.RESOLVED,
                    ComplaintState.REJECTED),
            ComplaintState.ON_HOLD, EnumSet.of(ComplaintState.IN_PROGRESS, ComplaintState.ASSIGNED,
                    ComplaintState.RESOLVED, ComplaintState.REJECTED),
            ComplaintState.RESOLVED, EnumSet.of(ComplaintState.CLOSED),
            ComplaintState.REJECTED, EnumSet.noneOf(ComplaintState.class),
            ComplaintState.CLOSED, EnumSet.noneOf(ComplaintState.class));

    private final WorkflowRepository workflowRepository;
    private final WorkflowTransitionRepository transitionRepository;
    private final UserAccountRepository userAccountRepository;
    private final SlaService slaService;
    private final AuditService auditService;

    public WorkflowService(WorkflowRepository workflowRepository,
            WorkflowTransitionRepository transitionRepository,
            UserAccountRepository userAccountRepository,
            SlaService slaService,
            AuditService auditService) {
        this.workflowRepository = workflowRepository;
        this.transitionRepository = transitionRepository;
        this.userAccountRepository = userAccountRepository;
        this.slaService = slaService;
        this.auditService = auditService;
    }

    @Transactional
    public void markRouted(Complaint complaint) {
        transition(complaint, ComplaintState.ROUTED, "Matched an active routing rule", null, true);
    }

    @Transactional
    public void markAssigned(Complaint complaint, String reason, UUID actorUserId) {
        Workflow workflow = workflowRepository.findByComplaintIdForUpdate(complaint.getComplaintId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint workflow not found"));
        if (workflow.getCurrentState() == ComplaintState.ASSIGNED) {
            return;
        }
        transition(complaint, ComplaintState.ASSIGNED, reason, actorUserId, false);
    }

    @Transactional
    public void transition(UUID complaintId, ComplaintState targetState, String reason, UUID actorUserId) {
        if (targetState == ComplaintState.ROUTED || targetState == ComplaintState.ASSIGNED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Routing and assignment states are changed by their dedicated operations");
        }
        Workflow workflow = workflowRepository.findByComplaintIdForUpdate(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint workflow not found"));
        Complaint complaint = java.util.Objects.requireNonNull(workflow.getComplaint(),
                "A workflow must reference its complaint");
        transition(complaint, targetState, reason, actorUserId, false);
    }

    @Transactional(readOnly = true)
    public Workflow getWorkflow(UUID complaintId) {
        return workflowRepository.findByComplaintComplaintId(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint workflow not found"));
    }

    @Transactional
    public Workflow lockForUpdate(UUID complaintId) {
        return workflowRepository.findByComplaintIdForUpdate(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint workflow not found"));
    }

    @Transactional(readOnly = true)
    public java.util.List<WorkflowTransition> getHistory(UUID complaintId) {
        Workflow workflow = getWorkflow(complaintId);
        return transitionRepository.findByWorkflowWorkflowIdOrderByCreatedAtAsc(workflow.getWorkflowId());
    }

    private void transition(Complaint complaint, ComplaintState targetState, String reason,
            UUID actorUserId, boolean routingTransition) {
        Workflow workflow = workflowRepository.findByComplaintIdForUpdate(complaint.getComplaintId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint workflow not found"));
        ComplaintState fromState = workflow.getCurrentState();
        if (!ALLOWED_TRANSITIONS.get(fromState).contains(targetState)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Complaint cannot transition from " + fromState + " to " + targetState);
        }
        if (targetState == ComplaintState.ROUTED && !routingTransition) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the routing engine can route complaints");
        }

        UserAccount actor = actorUserId == null
                ? null
                : userAccountRepository.findById(actorUserId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Actor account not found"));

        workflow.updateState(targetState);
        workflowRepository.save(workflow);
        transitionRepository.save(new WorkflowTransition(
                UUID.randomUUID(), workflow, fromState, targetState, reason, actor));

        if (targetState == ComplaintState.ON_HOLD) {
            slaService.pauseForHold(complaint);
        } else if (fromState == ComplaintState.ON_HOLD && targetState == ComplaintState.IN_PROGRESS) {
            slaService.resumeForWork(complaint);
        }
        if (targetState == ComplaintState.RESOLVED || targetState == ComplaintState.REJECTED
                || targetState == ComplaintState.CLOSED) {
            slaService.resolveForWorkflow(complaint);
        }

        auditService.log(
                "COMPLAINT_WORKFLOW_TRANSITION",
                actorUserId,
                complaint.getComplaintId(),
                "Complaint " + complaint.getComplaintReference() + " transitioned from "
                        + fromState + " to " + targetState);
    }
}
