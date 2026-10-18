package com.civictransparency.complaint.dto;

import com.civictransparency.workflow.entity.ComplaintState;

import java.util.List;
import java.util.UUID;

public record ComplaintStatusResponse(
        UUID complaintId,
        String complaintReference,
        ComplaintState currentState,
        List<CitizenStatusTransitionResponse> history) {
}
