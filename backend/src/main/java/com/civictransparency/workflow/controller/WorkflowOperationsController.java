package com.civictransparency.workflow.controller;

import com.civictransparency.workflow.dto.WorkflowOperationsDtos.AssignmentRequest;
import com.civictransparency.workflow.dto.WorkflowOperationsDtos.AssignmentResponse;
import com.civictransparency.workflow.dto.WorkflowOperationsDtos.SlaPauseRequest;
import com.civictransparency.workflow.dto.WorkflowOperationsDtos.SlaResponse;
import com.civictransparency.workflow.dto.WorkflowOperationsDtos.TransitionRequest;
import com.civictransparency.workflow.dto.WorkflowOperationsDtos.WorkflowResponse;
import com.civictransparency.workflow.dto.WorkflowOperationsDtos.WorkflowTransitionResponse;
import com.civictransparency.workflow.service.WorkflowOperationsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authority/complaints/{complaintId}")
@PreAuthorize("hasAnyRole('AUTHORITY_ADMIN', 'AUTHORITY_OFFICER', 'SYSTEM_ADMIN')")
public class WorkflowOperationsController {

    private final WorkflowOperationsService operationsService;

    public WorkflowOperationsController(WorkflowOperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @PostMapping("/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('AUTHORITY_ADMIN', 'SYSTEM_ADMIN')")
    public AssignmentResponse assign(@PathVariable UUID complaintId,
            @Valid @RequestBody AssignmentRequest request,
            @AuthenticationPrincipal UUID actorUserId) {
        return operationsService.assign(complaintId, request.departmentId(), request.reason(), actorUserId);
    }

    @GetMapping("/assignments")
    public List<AssignmentResponse> getAssignmentHistory(@PathVariable UUID complaintId,
            @AuthenticationPrincipal UUID actorUserId) {
        return operationsService.getAssignmentHistory(complaintId, actorUserId);
    }

    @GetMapping("/workflow")
    public WorkflowResponse getWorkflow(@PathVariable UUID complaintId,
            @AuthenticationPrincipal UUID actorUserId) {
        return operationsService.getWorkflow(complaintId, actorUserId);
    }

    @GetMapping("/workflow/history")
    public List<WorkflowTransitionResponse> getWorkflowHistory(@PathVariable UUID complaintId,
            @AuthenticationPrincipal UUID actorUserId) {
        return operationsService.getWorkflowHistory(complaintId, actorUserId);
    }

    @PostMapping("/workflow/transitions")
    public WorkflowResponse transition(@PathVariable UUID complaintId,
            @Valid @RequestBody TransitionRequest request,
            @AuthenticationPrincipal UUID actorUserId) {
        return operationsService.transition(complaintId, request.toState(), request.reason(), actorUserId);
    }

    @GetMapping("/sla")
    public SlaResponse getSla(@PathVariable UUID complaintId,
            @AuthenticationPrincipal UUID actorUserId) {
        return operationsService.getSla(complaintId, actorUserId);
    }

    @PostMapping("/sla/pause")
    public SlaResponse pauseSla(@PathVariable UUID complaintId,
            @Valid @RequestBody SlaPauseRequest request,
            @AuthenticationPrincipal UUID actorUserId) {
        return operationsService.pauseSla(complaintId, request.reason(), actorUserId);
    }

    @PostMapping("/sla/resume")
    public SlaResponse resumeSla(@PathVariable UUID complaintId,
            @AuthenticationPrincipal UUID actorUserId) {
        return operationsService.resumeSla(complaintId, actorUserId);
    }
}
