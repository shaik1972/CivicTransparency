package com.civictransparency.workflow.controller;

import com.civictransparency.workflow.dto.AuthorityComplaintResponse;
import com.civictransparency.workflow.service.WorkflowOperationsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authority/complaints")
@PreAuthorize("hasAnyRole('AUTHORITY_ADMIN', 'AUTHORITY_OFFICER', 'SYSTEM_ADMIN')")
public class AuthorityComplaintQueueController {

    private final WorkflowOperationsService operationsService;

    public AuthorityComplaintQueueController(WorkflowOperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @GetMapping
    public List<AuthorityComplaintResponse> getAssignedComplaints(
            @AuthenticationPrincipal UUID actorUserId) {
        return operationsService.getAssignedComplaints(actorUserId);
    }
}
