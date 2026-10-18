package com.civictransparency.workflow.controller;

import com.civictransparency.authority.repository.AuthorityDepartmentRepository;
import com.civictransparency.assignment.repository.ComplaintAssignmentRepository;
import com.civictransparency.auth.service.AccessTokenService;
import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.citizen.repository.CitizenProfileRepository;
import com.civictransparency.complaint.repository.ComplaintRepository;
import com.civictransparency.identity.entity.Role;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.entity.UserRole;
import com.civictransparency.identity.repository.RoleRepository;
import com.civictransparency.identity.repository.UserAccountRepository;
import com.civictransparency.identity.repository.UserRoleRepository;
import com.civictransparency.routing.repository.RoutingDecisionRepository;
import com.civictransparency.security.PasswordHashingService;
import com.civictransparency.sla.repository.ComplaintSlaRepository;
import com.civictransparency.workflow.entity.ComplaintState;
import com.civictransparency.workflow.repository.WorkflowRepository;
import com.civictransparency.workflow.repository.WorkflowTransitionRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkflowOperationsIntegrationTest {

    private static final UUID STATE_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID CITY_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID WARD_ID = UUID.fromString("c1000000-0000-4000-8000-000000000001");
    private static final UUID CATEGORY_ID = UUID.fromString("a1000000-0000-4000-8000-000000000001");
    private static final UUID SUBCATEGORY_ID = UUID.fromString("b1000000-0000-4000-8000-000000000001");
    private static final String PASSWORD = "AuthorityPass123!";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserAccountRepository userAccountRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private CitizenProfileRepository citizenProfileRepository;
    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private AuthorityDepartmentRepository departmentRepository;
    @Autowired private ComplaintAssignmentRepository assignmentRepository;
    @Autowired private RoutingDecisionRepository routingDecisionRepository;
    @Autowired private ComplaintSlaRepository complaintSlaRepository;
    @Autowired private WorkflowRepository workflowRepository;
    @Autowired private WorkflowTransitionRepository transitionRepository;
    @Autowired private PasswordHashingService passwordHashingService;
    @Autowired private AccessTokenService accessTokenService;

    @Test
    void routesAssignsTracksHistoryAndAppliesSlaPolicyWithDepartmentAuthorization() throws Exception {
        UserAccount systemAdmin = createUser("sysadmin", "SYSTEM_ADMIN", false);
        String adminToken = token(systemAdmin, "SYSTEM_ADMIN");
        String citizenToken;

        UserAccount citizen = createUser("citizen", "CITIZEN", true);
        citizenToken = token(citizen, "CITIZEN");

        mockMvc.perform(get("/api/v1/admin/routing-rules")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isForbidden());

        String authorityBody = mockMvc.perform(post("/api/v1/admin/authorities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"CITY-WORKS","name":"City Works Authority","stateId":"%s","cityId":"%s","status":"ACTIVE"}
                                """.formatted(STATE_ID, CITY_ID)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID authorityId = UUID.fromString(JsonPath.read(authorityBody, "$.authorityId"));

        UUID firstDepartmentId = createDepartment(adminToken, authorityId, "ROADS", "Roads Department");
        UUID secondDepartmentId = createDepartment(adminToken, authorityId, "PUBLIC-WORKS", "Public Works Department");
        createAuthorityUser(adminToken, "roads-officer", firstDepartmentId);
        createAuthorityUser(adminToken, "works-officer", secondDepartmentId);

        String officerLogin = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"roads-officer@example.com","password":"%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citizenId").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.roles[0]").value("AUTHORITY_OFFICER"))
                .andReturn().getResponse().getContentAsString();
        String firstOfficerToken = JsonPath.read(officerLogin, "$.accessToken");

        String secondOfficerLogin = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"works-officer@example.com","password":"%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String secondOfficerToken = JsonPath.read(secondOfficerLogin, "$.accessToken");

        createRoutingRule(adminToken, firstDepartmentId, 20, CITY_ID, WARD_ID);
        createRoutingRule(adminToken, secondDepartmentId, 1, null, null);
        createSlaPolicy(adminToken, SUBCATEGORY_ID, 24, true);
        createSlaPolicy(adminToken, null, 48, false);

        String complaintJson = """
                {
                    "citizenId": "%s",
                    "categoryId": "%s",
                    "subcategoryId": "%s",
                    "title": "Pothole on the main road",
                    "description": "The road surface has a deep pothole.",
                    "cityId": "%s",
                    "wardId": "%s"
                }
                """.formatted(citizenProfileRepository.findByUserUserId(citizen.getUserId()).orElseThrow().getCitizenId(),
                CATEGORY_ID, SUBCATEGORY_ID, CITY_ID, WARD_ID);
        String submission = mockMvc.perform(post("/api/v1/complaints")
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(complaintJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID complaintId = UUID.fromString(JsonPath.read(submission, "$.complaintId"));

        assertTrue(complaintRepository.findById(complaintId).isPresent());
        assertEquals(ComplaintState.ASSIGNED,
                workflowRepository.findByComplaintComplaintId(complaintId).orElseThrow().getCurrentState());
        assertEquals(firstDepartmentId, assignmentRepository
                .findByComplaint_ComplaintIdAndUnassignedAtIsNull(complaintId).orElseThrow()
                .getDepartment().getDepartmentId());
        assertEquals(1L, routingDecisionRepository.findAll().stream()
                .filter(decision -> decision.getComplaint().getComplaintId().equals(complaintId)).count());
        assertEquals("ACTIVE", complaintSlaRepository.findByComplaint_ComplaintId(complaintId).orElseThrow().getStatus());
        assertEquals(24, complaintSlaRepository.findByComplaint_ComplaintId(complaintId).orElseThrow()
                .getPolicy().getTargetHours());

        mockMvc.perform(get("/api/v1/authority/complaints")
                        .header("Authorization", "Bearer " + firstOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].complaintId").value(complaintId.toString()))
                .andExpect(jsonPath("$[0].description").value("The road surface has a deep pothole."));

        mockMvc.perform(get("/api/v1/authority/complaints")
                        .header("Authorization", "Bearer " + secondOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/authority/complaints")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/complaints/{id}/status", complaintId)
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentState").value("ASSIGNED"))
                .andExpect(jsonPath("$.history.length()").value(3));

        UserAccount otherCitizen = createUser("other-citizen", "CITIZEN", true);
        mockMvc.perform(get("/api/v1/complaints/{id}/status", complaintId)
                        .header("Authorization", "Bearer " + token(otherCitizen, "CITIZEN")))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/authority/complaints/{id}/workflow/transitions", complaintId)
                        .header("Authorization", "Bearer " + firstOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toState":"CLOSED","reason":"Invalid direct closure"}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/authority/complaints/{id}/assignments", complaintId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"departmentId":"%s","reason":"Manual cross-department reassignment"}
                                """.formatted(secondDepartmentId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.departmentId").value(secondDepartmentId.toString()));

        List<com.civictransparency.assignment.entity.ComplaintAssignment> assignments =
                assignmentRepository.findByComplaint_ComplaintIdOrderByAssignedAtAsc(complaintId);
        assertEquals(2, assignments.size());
        assertNotNull(assignments.get(0).getUnassignedAt());
        assertEquals(secondDepartmentId, assignments.get(1).getDepartment().getDepartmentId());

        mockMvc.perform(post("/api/v1/authority/complaints/{id}/workflow/transitions", complaintId)
                        .header("Authorization", "Bearer " + firstOfficerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toState":"IN_PROGRESS","reason":"Begin repair work"}
                                """))
                .andExpect(status().isForbidden());

        OffsetDateTime initialDeadline = complaintSlaRepository.findByComplaint_ComplaintId(complaintId)
                .orElseThrow().getDeadline();
        transition(complaintId, secondOfficerToken, "IN_PROGRESS", "Begin repair work");
        transition(complaintId, secondOfficerToken, "ON_HOLD", "Waiting for materials");
        assertTrue(complaintSlaRepository.findByComplaint_ComplaintId(complaintId).orElseThrow().getIsPaused());

        mockMvc.perform(post("/api/v1/authority/complaints/{id}/sla/resume", complaintId)
                        .header("Authorization", "Bearer " + secondOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paused").value(false));
        assertTrue(complaintSlaRepository.findByComplaint_ComplaintId(complaintId).orElseThrow()
                .getDeadline().isAfter(initialDeadline));

        transition(complaintId, secondOfficerToken, "IN_PROGRESS", "Work resumed");
        transition(complaintId, secondOfficerToken, "RESOLVED", "Pothole repaired");
        assertEquals("RESOLVED", complaintSlaRepository.findByComplaint_ComplaintId(complaintId).orElseThrow().getStatus());
        assertEquals(ComplaintState.RESOLVED,
                workflowRepository.findByComplaintComplaintId(complaintId).orElseThrow().getCurrentState());

        mockMvc.perform(get("/api/v1/authority/complaints/{id}/workflow/history", complaintId)
                        .header("Authorization", "Bearer " + secondOfficerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));

        mockMvc.perform(get("/api/v1/complaints/{id}/status", complaintId)
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentState").value("RESOLVED"))
                .andExpect(jsonPath("$.history.length()").value(7));

        assertFalse(transitionRepository.findByWorkflowWorkflowIdOrderByCreatedAtAsc(
                workflowRepository.findByComplaintComplaintId(complaintId).orElseThrow().getWorkflowId()).isEmpty());
        assertTrue(departmentRepository.findById(secondDepartmentId).isPresent());
    }

    private UUID createDepartment(String token, UUID authorityId, String code, String name) throws Exception {
        String response = mockMvc.perform(post("/api/v1/admin/departments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorityId":"%s","code":"%s","name":"%s"}
                                """.formatted(authorityId, code, name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(response, "$.departmentId"));
    }

    private void createAuthorityUser(String token, String username, UUID departmentId) throws Exception {
        mockMvc.perform(post("/api/v1/admin/authority-users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s@example.com","username":"%s","password":"%s","departmentId":"%s","employeeReference":"EMP-%s","roleCode":"AUTHORITY_OFFICER"}
                                """.formatted(username, username, PASSWORD, departmentId, username)))
                .andExpect(status().isCreated());
    }

    private void createRoutingRule(String token, UUID departmentId, int priority, UUID cityId, UUID wardId)
            throws Exception {
        String cityJson = cityId == null ? "null" : "\"" + cityId + "\"";
        String wardJson = wardId == null ? "null" : "\"" + wardId + "\"";
        mockMvc.perform(post("/api/v1/admin/routing-rules")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Road damage rule %d","stateId":"%s","cityId":%s,"wardId":%s,"categoryId":"%s","subcategoryId":"%s","severity":"HIGH","departmentId":"%s","priority":%d,"active":true}
                                """.formatted(priority, STATE_ID, cityJson, wardJson, CATEGORY_ID, SUBCATEGORY_ID,
                                departmentId, priority)))
                .andExpect(status().isCreated());
    }

    private void createSlaPolicy(String token, UUID subcategoryId, int targetHours, boolean holdPausesSla)
            throws Exception {
        String subcategoryJson = subcategoryId == null ? "null" : "\"" + subcategoryId + "\"";
        mockMvc.perform(post("/api/v1/admin/sla-policies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":"%s","subcategoryId":%s,"severity":"HIGH","targetHours":%d,"active":true,"holdPausesSla":%s}
                                """.formatted(CATEGORY_ID, subcategoryJson, targetHours, holdPausesSla)))
                .andExpect(status().isCreated());
    }

    private void transition(UUID complaintId, String token, String state, String reason) throws Exception {
        mockMvc.perform(post("/api/v1/authority/complaints/{id}/workflow/transitions", complaintId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toState":"%s","reason":"%s"}
                                """.formatted(state, reason)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentState").value(state));
    }

    private UserAccount createUser(String suffix, String roleCode, boolean citizenProfile) {
        UserAccount account = new UserAccount(suffix + "." + UUID.randomUUID() + "@example.com", "ACTIVE");
        account.setUsername(suffix + "-" + UUID.randomUUID());
        account.setPasswordHash(passwordHashingService.hash(PASSWORD));
        account = userAccountRepository.save(account);

        Role role = roleRepository.findByCode(roleCode).orElseThrow();
        userRoleRepository.save(new UserRole(account, role));
        if (citizenProfile) {
            citizenProfileRepository.save(new CitizenProfile(account, "Workflow Test Citizen", "ACTIVE"));
        }
        return account;
    }

    private String token(UserAccount account, String roleCode) {
        return accessTokenService.issue(account.getUserId(), List.of(roleCode), OffsetDateTime.now().plusHours(1));
    }
}
