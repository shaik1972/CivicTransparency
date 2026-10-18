package com.civictransparency.complaint.dto;

import com.civictransparency.workflow.entity.ComplaintState;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CitizenStatusTransitionResponse(
        UUID transitionId,
        ComplaintState fromState,
        ComplaintState toState,
        String reason,
        OffsetDateTime createdAt) {
}
