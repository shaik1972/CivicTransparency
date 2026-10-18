package com.civictransparency.workflow.dto;

import com.civictransparency.complaint.entity.Severity;
import com.civictransparency.workflow.entity.ComplaintState;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthorityComplaintResponse(
        UUID complaintId,
        String complaintReference,
        String title,
        String description,
        String category,
        Severity severity,
        ComplaintState state,
        String city,
        OffsetDateTime createdAt,
        UUID departmentId,
        String departmentName) {
}
