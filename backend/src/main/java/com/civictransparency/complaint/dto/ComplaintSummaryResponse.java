package com.civictransparency.complaint.dto;

import com.civictransparency.complaint.entity.Severity;
import com.civictransparency.workflow.entity.ComplaintState;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ComplaintSummaryResponse(
        UUID complaintId,
        String complaintReference,
        String title,
        String category,
        Severity severity,
        ComplaintState state,
        String city,
        OffsetDateTime createdAt) {
}
