package com.civictransparency.complaint.controller;

import com.civictransparency.auth.exception.InvalidAccessTokenException;
import com.civictransparency.auth.service.AccessTokenService;
import com.civictransparency.citizen.repository.CitizenProfileRepository;
import com.civictransparency.complaint.dto.ComplaintStatusResponse;
import com.civictransparency.complaint.dto.CitizenStatusTransitionResponse;
import com.civictransparency.complaint.dto.ComplaintSummaryResponse;
import com.civictransparency.complaint.dto.ComplaintSubmissionRequest;
import com.civictransparency.complaint.repository.ComplaintRepository;
import com.civictransparency.complaint.service.ComplaintSubmissionService;
import com.civictransparency.workflow.repository.WorkflowRepository;
import com.civictransparency.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/complaints")
public class ComplaintController {

    private final ComplaintSubmissionService submissionService;
    private final AccessTokenService accessTokenService;
    private final CitizenProfileRepository citizenProfileRepository;
    private final ComplaintRepository complaintRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowService workflowService;

    public ComplaintController(
            ComplaintSubmissionService submissionService,
            AccessTokenService accessTokenService,
            CitizenProfileRepository citizenProfileRepository,
            ComplaintRepository complaintRepository,
            WorkflowRepository workflowRepository,
            WorkflowService workflowService) {
        this.submissionService = submissionService;
        this.accessTokenService = accessTokenService;
        this.citizenProfileRepository = citizenProfileRepository;
        this.complaintRepository = complaintRepository;
        this.workflowRepository = workflowRepository;
        this.workflowService = workflowService;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> submitComplaint(
            @Valid @RequestBody ComplaintSubmissionRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new InvalidAccessTokenException();
        }
        UUID userId = accessTokenService.verify(authorization.substring("Bearer ".length()).trim());
        UUID citizenUserId = citizenProfileRepository.findById(request.citizenId())
                .map(profile -> profile.getUser().getUserId())
                .orElseThrow(InvalidAccessTokenException::new);
        if (!userId.equals(citizenUserId)) {
            throw new InvalidAccessTokenException();
        }

        UUID complaintId = submissionService.submitComplaint(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("complaintId", complaintId));
    }

    @GetMapping("/mine")
    public List<ComplaintSummaryResponse> getMyComplaints(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        UUID userId = authenticate(authorization);
        UUID citizenId = citizenProfileRepository.findByUserUserId(userId)
                .orElseThrow(InvalidAccessTokenException::new)
                .getCitizenId();

        return complaintRepository.findByCitizenCitizenIdOrderByCreatedAtDesc(citizenId)
                .stream()
                .map(complaint -> new ComplaintSummaryResponse(
                        complaint.getComplaintId(),
                        complaint.getComplaintReference(),
                        complaint.getTitle(),
                        complaint.getCategory().getName(),
                        complaint.getSeverity(),
                        workflowRepository.findByComplaintComplaintId(complaint.getComplaintId())
                                .map(workflow -> workflow.getCurrentState())
                                .orElse(null),
                        complaint.getCity().getName(),
                        complaint.getCreatedAt()))
                .toList();
    }

    @GetMapping("/{complaintId}/status")
    public ComplaintStatusResponse getComplaintStatus(
            @PathVariable UUID complaintId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        UUID userId = authenticate(authorization);
        var complaint = complaintRepository.findByComplaintIdAndCitizen_User_UserId(complaintId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint was not found"));

        var workflow = workflowService.getWorkflow(complaintId);
        var history = workflowService.getHistory(complaintId).stream()
                .map(transition -> new CitizenStatusTransitionResponse(
                        transition.getTransitionId(),
                        transition.getFromState(),
                        transition.getToState(),
                        transition.getReason(),
                        transition.getCreatedAt()))
                .toList();
        return new ComplaintStatusResponse(
                complaintId, complaint.getComplaintReference(), workflow.getCurrentState(), history);
    }

    private UUID authenticate(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new InvalidAccessTokenException();
        }
        return accessTokenService.verify(authorization.substring("Bearer ".length()).trim());
    }
}
