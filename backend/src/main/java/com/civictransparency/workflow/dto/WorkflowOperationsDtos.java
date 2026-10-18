package com.civictransparency.workflow.dto;

import com.civictransparency.workflow.entity.ComplaintState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class WorkflowOperationsDtos {

    private WorkflowOperationsDtos() {
    }

    public record AssignmentRequest(
            @NotNull UUID departmentId,
            @NotBlank @Size(max = 50) String reason) {
    }

    public record TransitionRequest(
            @NotNull ComplaintState toState,
            @NotBlank @Size(max = 1000) String reason) {
    }

    public record SlaPauseRequest(@NotBlank @Size(max = 200) String reason) {
    }

    public record AssignmentResponse(UUID assignmentId, UUID complaintId, UUID departmentId, String departmentName,
            OffsetDateTime assignedAt, OffsetDateTime unassignedAt, String reason, UUID assignedBy) {
    }

    public record WorkflowResponse(UUID complaintId, ComplaintState currentState, OffsetDateTime startedAt,
            OffsetDateTime completedAt, Long version) {
    }

    public record WorkflowTransitionResponse(UUID transitionId, ComplaintState fromState, ComplaintState toState,
            String reason, UUID actorUserId, OffsetDateTime createdAt) {
    }

    public record SlaResponse(UUID slaId, UUID policyId, Integer targetHours, OffsetDateTime deadline,
            Boolean paused, OffsetDateTime pausedAt, String pausedReason, OffsetDateTime resolvedAt, String status) {
    }
}
