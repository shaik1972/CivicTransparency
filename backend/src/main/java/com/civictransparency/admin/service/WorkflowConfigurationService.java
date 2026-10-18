package com.civictransparency.admin.service;

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
import com.civictransparency.authority.entity.Authority;
import com.civictransparency.authority.entity.AuthorityDepartment;
import com.civictransparency.authority.entity.AuthorityUser;
import com.civictransparency.authority.repository.AuthorityDepartmentRepository;
import com.civictransparency.authority.repository.AuthorityRepository;
import com.civictransparency.authority.repository.AuthorityUserRepository;
import com.civictransparency.complaint.entity.ComplaintCategory;
import com.civictransparency.complaint.entity.ComplaintSubcategory;
import com.civictransparency.complaint.repository.ComplaintCategoryRepository;
import com.civictransparency.complaint.repository.ComplaintSubcategoryRepository;
import com.civictransparency.geography.entity.City;
import com.civictransparency.geography.entity.State;
import com.civictransparency.geography.entity.Ward;
import com.civictransparency.geography.repository.CityRepository;
import com.civictransparency.geography.repository.StateRepository;
import com.civictransparency.geography.repository.WardRepository;
import com.civictransparency.identity.entity.Role;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.entity.UserRole;
import com.civictransparency.identity.repository.RoleRepository;
import com.civictransparency.identity.repository.UserAccountRepository;
import com.civictransparency.identity.repository.UserRoleRepository;
import com.civictransparency.routing.entity.RoutingRule;
import com.civictransparency.routing.repository.RoutingRuleRepository;
import com.civictransparency.security.PasswordHashingService;
import com.civictransparency.sla.entity.SlaPolicy;
import com.civictransparency.sla.repository.SlaPolicyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class WorkflowConfigurationService {

    private final AuthorityRepository authorityRepository;
    private final AuthorityDepartmentRepository departmentRepository;
    private final AuthorityUserRepository authorityUserRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordHashingService passwordHashingService;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final WardRepository wardRepository;
    private final ComplaintCategoryRepository categoryRepository;
    private final ComplaintSubcategoryRepository subcategoryRepository;
    private final RoutingRuleRepository routingRuleRepository;
    private final SlaPolicyRepository slaPolicyRepository;

    public WorkflowConfigurationService(AuthorityRepository authorityRepository,
            AuthorityDepartmentRepository departmentRepository,
            AuthorityUserRepository authorityUserRepository,
            UserAccountRepository userAccountRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            PasswordHashingService passwordHashingService,
            StateRepository stateRepository,
            CityRepository cityRepository,
            WardRepository wardRepository,
            ComplaintCategoryRepository categoryRepository,
            ComplaintSubcategoryRepository subcategoryRepository,
            RoutingRuleRepository routingRuleRepository,
            SlaPolicyRepository slaPolicyRepository) {
        this.authorityRepository = authorityRepository;
        this.departmentRepository = departmentRepository;
        this.authorityUserRepository = authorityUserRepository;
        this.userAccountRepository = userAccountRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordHashingService = passwordHashingService;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.wardRepository = wardRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.routingRuleRepository = routingRuleRepository;
        this.slaPolicyRepository = slaPolicyRepository;
    }

    @Transactional
    public AuthorityResponse createAuthority(AuthorityRequest request) {
        if (request.code() != null && !request.code().isBlank()
                && authorityRepository.existsByCode(request.code().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Authority code is already in use");
        }
        State state = request.stateId() == null ? null : require(stateRepository.findById(request.stateId()), "State");
        City city = request.cityId() == null ? null : require(cityRepository.findById(request.cityId()), "City");
        if (city != null && state != null && !city.getState().getStateId().equals(state.getStateId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City does not belong to the selected state");
        }
        if (city != null && state == null) {
            state = city.getState();
        }
        Authority authority = authorityRepository.save(new Authority(
                UUID.randomUUID(), request.code() == null ? null : request.code().trim(),
                request.name().trim(), state, city, request.status().trim().toUpperCase()));
        return authorityResponse(authority);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Authority authority = require(authorityRepository.findById(request.authorityId()), "Authority");
        if (!"ACTIVE".equals(authority.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot add a department to an inactive authority");
        }
        if (departmentRepository.findByCodeAndAuthority_AuthorityId(request.code(), authority.getAuthorityId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department code already exists for this authority");
        }
        AuthorityDepartment department = departmentRepository.save(new AuthorityDepartment(
                UUID.randomUUID(), authority, request.code(), request.name(), "ACTIVE"));
        return departmentResponse(department);
    }

    @Transactional
    public AuthorityUserResponse createAuthorityUser(AuthorityUserRequest request) {
        AuthorityDepartment department = require(departmentRepository.findById(request.departmentId()), "Department");
        if (!"ACTIVE".equals(department.getStatus()) || !"ACTIVE".equals(department.getAuthority().getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot add an officer to an inactive department");
        }
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userAccountRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already uses this email address");
        }
        if (userAccountRepository.existsByUsername(request.username().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }

        String roleCode = request.roleCode();
        Role role = require(roleRepository.findByCode(roleCode), "Authority role");
        UserAccount account = new UserAccount(email, "ACTIVE");
        account.setUsername(request.username().trim());
        account.setPasswordHash(passwordHashingService.hash(request.password()));
        account = userAccountRepository.save(account);
        userRoleRepository.save(new UserRole(account, role));

        AuthorityUser authorityUser = authorityUserRepository.save(new AuthorityUser(
                UUID.randomUUID(), account, department, request.employeeReference()));
        return authorityUserResponse(authorityUser, roleCode);
    }

    @Transactional
    public RoutingRuleResponse createRoutingRule(RoutingRuleRequest request) {
        State state = request.stateId() == null ? null : require(stateRepository.findById(request.stateId()), "State");
        City city = request.cityId() == null ? null : require(cityRepository.findById(request.cityId()), "City");
        Ward ward = request.wardId() == null ? null : require(wardRepository.findById(request.wardId()), "Ward");
        ComplaintCategory category = request.categoryId() == null ? null
                : require(categoryRepository.findById(request.categoryId()), "Category");
        ComplaintSubcategory subcategory = request.subcategoryId() == null ? null
                : require(subcategoryRepository.findById(request.subcategoryId()), "Subcategory");
        AuthorityDepartment department = require(departmentRepository.findById(request.departmentId()), "Department");

        if (city != null && state != null && !city.getState().getStateId().equals(state.getStateId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City does not belong to the selected state");
        }
        if (ward != null && city != null && !ward.getCity().getCityId().equals(city.getCityId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ward does not belong to the selected city");
        }
        if (ward != null && state != null
                && !ward.getCity().getState().getStateId().equals(state.getStateId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ward does not belong to the selected state");
        }
        if (subcategory != null && (category == null
                || !subcategory.getCategory().getCategoryId().equals(category.getCategoryId()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subcategory must belong to the selected category");
        }
        if (!"ACTIVE".equals(department.getStatus()) || !"ACTIVE".equals(department.getAuthority().getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Routing rules require an active department");
        }
        validateEffectiveDates(request.effectiveFrom(), request.effectiveTo());

        RoutingRule rule = routingRuleRepository.save(new RoutingRule(
                UUID.randomUUID(), request.name().trim(), state, city, ward, category, subcategory,
                request.severity(), department, request.priority(), request.active(),
                request.effectiveFrom(), request.effectiveTo()));
        return routingRuleResponse(rule);
    }

    @Transactional
    public SlaPolicyResponse createSlaPolicy(SlaPolicyRequest request) {
        ComplaintCategory category = require(categoryRepository.findById(request.categoryId()), "Category");
        ComplaintSubcategory subcategory = request.subcategoryId() == null ? null
                : require(subcategoryRepository.findById(request.subcategoryId()), "Subcategory");
        if (subcategory != null && !subcategory.getCategory().getCategoryId().equals(category.getCategoryId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subcategory does not belong to the selected category");
        }
        if (Boolean.TRUE.equals(request.active()) && hasActivePolicy(category.getCategoryId(),
                subcategory == null ? null : subcategory.getSubcategoryId(), request.severity().name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active SLA policy already exists for this match");
        }

        SlaPolicy policy = slaPolicyRepository.save(new SlaPolicy(
                UUID.randomUUID(), category, subcategory, request.severity().name(), request.targetHours(),
                request.active(), request.holdPausesSla()));
        return slaPolicyResponse(policy);
    }

    @Transactional(readOnly = true)
    public List<AuthorityResponse> getAuthorities() {
        return authorityRepository.findAll().stream().map(WorkflowConfigurationService::authorityResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartments() {
        return departmentRepository.findAll().stream().map(WorkflowConfigurationService::departmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuthorityUserResponse> getAuthorityUsers() {
        return authorityUserRepository.findAll().stream().map(user -> {
            String roleCode = userRoleRepository.findByIdUserId(user.getUserAccount().getUserId()).stream()
                    .map(userRole -> userRole.getRole().getCode())
                    .filter(code -> code.equals("AUTHORITY_ADMIN") || code.equals("AUTHORITY_OFFICER"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Authority user has no authority role"));
            return authorityUserResponse(user, roleCode);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<RoutingRuleResponse> getRoutingRules() {
        return routingRuleRepository.findAll().stream().map(WorkflowConfigurationService::routingRuleResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SlaPolicyResponse> getSlaPolicies() {
        return slaPolicyRepository.findAll().stream().map(WorkflowConfigurationService::slaPolicyResponse).toList();
    }

    @Transactional
    public AuthorityResponse updateAuthorityStatus(UUID authorityId, StatusRequest request) {
        Authority authority = require(authorityRepository.findById(authorityId), "Authority");
        authority.setStatus(request.status().trim().toUpperCase());
        return authorityResponse(authorityRepository.save(authority));
    }

    @Transactional
    public DepartmentResponse updateDepartmentStatus(UUID departmentId, StatusRequest request) {
        AuthorityDepartment department = require(departmentRepository.findById(departmentId), "Department");
        department.setStatus(request.status().trim().toUpperCase());
        return departmentResponse(departmentRepository.save(department));
    }

    @Transactional
    public RoutingRuleResponse updateRoutingRuleActive(UUID ruleId, ActiveRequest request) {
        RoutingRule rule = require(routingRuleRepository.findById(ruleId), "Routing rule");
        if (Boolean.TRUE.equals(request.active())
                && (!"ACTIVE".equals(rule.getDepartment().getStatus())
                        || !"ACTIVE".equals(rule.getDepartment().getAuthority().getStatus()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot activate a rule for an inactive department");
        }
        rule.setIsActive(request.active());
        return routingRuleResponse(routingRuleRepository.save(rule));
    }

    @Transactional
    public SlaPolicyResponse updateSlaPolicyActive(UUID policyId, ActiveRequest request) {
        SlaPolicy policy = require(slaPolicyRepository.findById(policyId), "SLA policy");
        if (Boolean.TRUE.equals(request.active()) && !Boolean.TRUE.equals(policy.getIsActive())
                && hasActivePolicy(policy.getCategory().getCategoryId(),
                        policy.getSubcategory() == null ? null : policy.getSubcategory().getSubcategoryId(),
                        policy.getSeverity())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active SLA policy already exists for this match");
        }
        policy.setIsActive(request.active());
        return slaPolicyResponse(slaPolicyRepository.save(policy));
    }

    private boolean hasActivePolicy(UUID categoryId, UUID subcategoryId, String severity) {
        return subcategoryId == null
                ? slaPolicyRepository.existsByCategory_CategoryIdAndSeverityAndIsActiveTrueAndSubcategoryIsNull(
                        categoryId, severity)
                : slaPolicyRepository.existsByCategory_CategoryIdAndSeverityAndIsActiveTrueAndSubcategory_SubcategoryId(
                        categoryId, severity, subcategoryId);
    }

    private static void validateEffectiveDates(OffsetDateTime from, OffsetDateTime to) {
        if (from != null && to != null && !to.isAfter(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "effectiveTo must be later than effectiveFrom");
        }
    }

    private static <T> T require(java.util.Optional<T> value, String entityName) {
        return value.orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, entityName + " was not found"));
    }

    private static AuthorityResponse authorityResponse(Authority authority) {
        return new AuthorityResponse(authority.getAuthorityId(), authority.getCode(), authority.getName(),
                authority.getState() == null ? null : authority.getState().getStateId(),
                authority.getCity() == null ? null : authority.getCity().getCityId(), authority.getStatus());
    }

    private static DepartmentResponse departmentResponse(AuthorityDepartment department) {
        return new DepartmentResponse(department.getDepartmentId(), department.getAuthority().getAuthorityId(),
                department.getCode(), department.getName(), department.getStatus());
    }

    private static AuthorityUserResponse authorityUserResponse(AuthorityUser user, String roleCode) {
        return new AuthorityUserResponse(user.getAuthorityUserId(), user.getUserAccount().getUserId(),
                user.getDepartment().getDepartmentId(), user.getUserAccount().getEmail(),
                user.getEmployeeReference(), roleCode);
    }

    private static RoutingRuleResponse routingRuleResponse(RoutingRule rule) {
        return new RoutingRuleResponse(rule.getRuleId(), rule.getName(),
                rule.getState() == null ? null : rule.getState().getStateId(),
                rule.getCity() == null ? null : rule.getCity().getCityId(),
                rule.getWard() == null ? null : rule.getWard().getWardId(),
                rule.getCategory() == null ? null : rule.getCategory().getCategoryId(),
                rule.getSubcategory() == null ? null : rule.getSubcategory().getSubcategoryId(),
                rule.getSeverity(), rule.getDepartment().getDepartmentId(), rule.getPriority(),
                rule.getIsActive(), rule.getEffectiveFrom(), rule.getEffectiveTo());
    }

    private static SlaPolicyResponse slaPolicyResponse(SlaPolicy policy) {
        return new SlaPolicyResponse(policy.getPolicyId(), policy.getCategory().getCategoryId(),
                policy.getSubcategory() == null ? null : policy.getSubcategory().getSubcategoryId(),
                com.civictransparency.complaint.entity.Severity.valueOf(policy.getSeverity()),
                policy.getTargetHours(), policy.getIsActive(), policy.getHoldPausesSla());
    }
}
