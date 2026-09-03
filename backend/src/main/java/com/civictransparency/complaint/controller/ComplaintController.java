package com.civictransparency.complaint.controller;

import com.civictransparency.complaint.dto.ComplaintSubmissionRequest;
import com.civictransparency.complaint.service.ComplaintSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/complaints")
public class ComplaintController {

    private final ComplaintSubmissionService submissionService;

    public ComplaintController(ComplaintSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> submitComplaint(
            @Valid @RequestBody ComplaintSubmissionRequest request) {

        UUID complaintId = submissionService.submitComplaint(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("complaintId", complaintId));
    }
}
