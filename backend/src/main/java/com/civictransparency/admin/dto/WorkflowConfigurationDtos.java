package com.civictransparency.admin.dto;

import com.civictransparency.complaint.entity.Severity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class WorkflowConfigurationDtos {

    private WorkflowConfigurationDtos() {
    }

    public record AuthorityRequest(
            @Size(max = 50) String code,
            @NotBlank @Size(max = 200) String name,
            UUID stateId,
            UUID cityId,
            @NotBlank @Size(max = 30) @Pattern(regexp = "(?i)ACTIVE|INACTIVE") String status) {
    }

    public record DepartmentRequest(
            @NotNull UUID authorityId,
            @NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 200) String name) {
    }

    public record AuthorityUserRequest(
            @Email @NotBlank @Size(max = 320) String email,
            @NotBlank @Size(max = 100) String username,
            @NotBlank @Size(min = 12, max = 128) String password,
            @NotNull UUID departmentId,
            @Size(max = 100) String employeeReference,
            @NotBlank @Pattern(regexp = "AUTHORITY_ADMIN|AUTHORITY_OFFICER") String roleCode) {
    }

    public record RoutingRuleRequest(
            @NotBlank @Size(max = 200) String name,
            UUID stateId,
            UUID cityId,
            UUID wardId,
            UUID categoryId,
            UUID subcategoryId,
            Severity severity,
            @NotNull UUID departmentId,
            @NotNull Integer priority,
            @NotNull Boolean active,
            OffsetDateTime effectiveFrom,
            OffsetDateTime effectiveTo) {
    }

    public record SlaPolicyRequest(
            @NotNull UUID categoryId,
            UUID subcategoryId,
            @NotNull Severity severity,
            @NotNull @Positive Integer targetHours,
            @NotNull Boolean active,
            @NotNull Boolean holdPausesSla) {
    }

    public record StatusRequest(
            @NotBlank @Pattern(regexp = "(?i)ACTIVE|INACTIVE") String status) {
    }

    public record ActiveRequest(@NotNull Boolean active) {
    }

    public record AuthorityResponse(UUID authorityId, String code, String name, UUID stateId, UUID cityId,
            String status) {
    }

    public record DepartmentResponse(UUID departmentId, UUID authorityId, String code, String name, String status) {
    }

    public record AuthorityUserResponse(UUID authorityUserId, UUID userId, UUID departmentId, String email,
            String employeeReference, String roleCode) {
    }

    public record RoutingRuleResponse(UUID ruleId, String name, UUID stateId, UUID cityId, UUID wardId,
            UUID categoryId, UUID subcategoryId, Severity severity, UUID departmentId, Integer priority,
            Boolean active, OffsetDateTime effectiveFrom, OffsetDateTime effectiveTo) {
    }

    public record SlaPolicyResponse(UUID policyId, UUID categoryId, UUID subcategoryId, Severity severity,
            Integer targetHours, Boolean active, Boolean holdPausesSla) {
    }
}
