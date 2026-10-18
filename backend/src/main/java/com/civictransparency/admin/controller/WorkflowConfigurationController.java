package com.civictransparency.admin.controller;

import com.civictransparency.admin.dto.WorkflowConfigurationDtos.AuthorityRequest;
import com.civictransparency.admin.dto.WorkflowConfigurationDtos.AuthorityResponse;
import com.civictransparency.admin.dto.WorkflowConfigurationDtos.AuthorityUserRequest;
import com.civictransparency.admin.dto.WorkflowConfigurationDtos.AuthorityUserResponse;
import com.civictransparency.admin.dto.WorkflowConfigurationDtos.ActiveRequest;
import com.civictransparency.admin.dto.WorkflowConfigurationDtos.DepartmentRequest;
import com.civictransparency.admin.dto.WorkflowConfigurationDtos.DepartmentResponse;
import com.civictransparency.admin.dto.WorkflowConfigurationDtos.StatusRequest;
import com.civictransparency.admin.dto.WorkflowConfigurationDtos.RoutingRuleRequest;
import com.civictransparency.admin.dto.WorkflowConfigurationDtos.RoutingRuleResponse;
import com.civictransparency.admin.dto.WorkflowConfigurationDtos.SlaPolicyRequest;
import com.civictransparency.admin.dto.WorkflowConfigurationDtos.SlaPolicyResponse;
import com.civictransparency.admin.service.WorkflowConfigurationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class WorkflowConfigurationController {

    private final WorkflowConfigurationService configurationService;

    public WorkflowConfigurationController(WorkflowConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostMapping("/authorities")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorityResponse createAuthority(@Valid @RequestBody AuthorityRequest request) {
        return configurationService.createAuthority(request);
    }

    @GetMapping("/authorities")
    public List<AuthorityResponse> getAuthorities() {
        return configurationService.getAuthorities();
    }

    @PatchMapping("/authorities/{authorityId}/status")
    public AuthorityResponse updateAuthorityStatus(@PathVariable java.util.UUID authorityId,
            @Valid @RequestBody StatusRequest request) {
        return configurationService.updateAuthorityStatus(authorityId, request);
    }

    @PostMapping("/departments")
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return configurationService.createDepartment(request);
    }

    @GetMapping("/departments")
    public List<DepartmentResponse> getDepartments() {
        return configurationService.getDepartments();
    }

    @PatchMapping("/departments/{departmentId}/status")
    public DepartmentResponse updateDepartmentStatus(@PathVariable java.util.UUID departmentId,
            @Valid @RequestBody StatusRequest request) {
        return configurationService.updateDepartmentStatus(departmentId, request);
    }

    @PostMapping("/authority-users")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorityUserResponse createAuthorityUser(@Valid @RequestBody AuthorityUserRequest request) {
        return configurationService.createAuthorityUser(request);
    }

    @GetMapping("/authority-users")
    public List<AuthorityUserResponse> getAuthorityUsers() {
        return configurationService.getAuthorityUsers();
    }

    @PostMapping("/routing-rules")
    @ResponseStatus(HttpStatus.CREATED)
    public RoutingRuleResponse createRoutingRule(@Valid @RequestBody RoutingRuleRequest request) {
        return configurationService.createRoutingRule(request);
    }

    @GetMapping("/routing-rules")
    public List<RoutingRuleResponse> getRoutingRules() {
        return configurationService.getRoutingRules();
    }

    @PatchMapping("/routing-rules/{ruleId}/active")
    public RoutingRuleResponse updateRoutingRuleActive(@PathVariable java.util.UUID ruleId,
            @Valid @RequestBody ActiveRequest request) {
        return configurationService.updateRoutingRuleActive(ruleId, request);
    }

    @PostMapping("/sla-policies")
    @ResponseStatus(HttpStatus.CREATED)
    public SlaPolicyResponse createSlaPolicy(@Valid @RequestBody SlaPolicyRequest request) {
        return configurationService.createSlaPolicy(request);
    }

    @GetMapping("/sla-policies")
    public List<SlaPolicyResponse> getSlaPolicies() {
        return configurationService.getSlaPolicies();
    }

    @PatchMapping("/sla-policies/{policyId}/active")
    public SlaPolicyResponse updateSlaPolicyActive(@PathVariable java.util.UUID policyId,
            @Valid @RequestBody ActiveRequest request) {
        return configurationService.updateSlaPolicyActive(policyId, request);
    }
}
