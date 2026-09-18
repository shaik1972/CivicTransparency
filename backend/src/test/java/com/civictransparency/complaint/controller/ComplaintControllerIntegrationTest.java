package com.civictransparency.complaint.controller;

import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.citizen.repository.CitizenProfileRepository;
import com.civictransparency.auth.service.AccessTokenService;
import com.civictransparency.complaint.repository.ComplaintRepository;
import com.civictransparency.evidence.repository.EvidenceRepository;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.repository.UserAccountRepository;
import com.civictransparency.location.repository.ComplaintLocationRepository;
import com.civictransparency.workflow.repository.WorkflowRepository;
import com.civictransparency.workflow.entity.ComplaintState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ComplaintControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CitizenProfileRepository citizenProfileRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ComplaintLocationRepository locationRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private EvidenceRepository evidenceRepository;

    @Autowired
    private AccessTokenService accessTokenService;

    @Test
    void submitComplaint_createsAllEntitiesAtomically() throws Exception {

        UserAccount userAccount = new UserAccount("complaint.test@example.com", "ACTIVE");
        userAccount.setUsername("complaint-test-user");
        userAccount = userAccountRepository.save(userAccount);

        CitizenProfile citizenProfile = new CitizenProfile(userAccount, "Complaint Test Citizen", "ACTIVE");
        citizenProfile = citizenProfileRepository.save(citizenProfile);

        String citizenId = citizenProfile.getCitizenId().toString();
        String categoryId = "a1000000-0000-4000-8000-000000000001"; // ROADS
        String subcategoryId = "b1000000-0000-4000-8000-000000000001"; // Pothole / Damaged Road Surface
        String cityId = "22222222-2222-4222-8222-222222222222"; // Bengaluru
        String wardId = "c1000000-0000-4000-8000-000000000001"; // Koramangala

        String requestJson = """
                    {
                        "citizenId": "%s",
                        "categoryId": "%s",
                        "subcategoryId": "%s",
                        "title": "Huge pothole on 100ft road",
                        "description": "It has been here for weeks and is causing traffic issues.",
                        "cityId": "%s",
                        "wardId": "%s",
                        "latitude": 12.9352,
                        "longitude": 77.6245,
                        "accuracyMeters": 5.0,
                        "locationDescription": "Opposite Sony World signal",
                        "evidence": [
                            {
                                "storageKey": "s3://civic-bucket/pothole1.jpg",
                                "mediaType": "image/jpeg",
                                "fileSizeBytes": 102400,
                                "sha256": "fakehash"
                            }
                        ]
                    }
                """.formatted(citizenId, categoryId, subcategoryId, cityId, wardId);

        String responseBody = mockMvc.perform(post("/api/v1/complaints")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessTokenService.issue(
                        userAccount.getUserId(), List.of("CITIZEN"), OffsetDateTime.now().plusHours(1)))
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.complaintId", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        // Extract UUID from response
        String complaintIdStr = responseBody.replaceAll(".*\"complaintId\":\"([^\"]+)\".*", "$1");
        UUID complaintId = UUID.fromString(complaintIdStr);

        // Verify Complaint
        var complaintOpt = complaintRepository.findById(complaintId);
        assertTrue(complaintOpt.isPresent());
        assertEquals("HIGH", complaintOpt.get().getSeverity().name());
        assertNotNull(complaintOpt.get().getComplaintReference());

        // Verify Location
        var locOpt = locationRepository.findByComplaintComplaintId(complaintId);
        assertTrue(locOpt.isPresent());
        assertNotNull(locOpt.get().getPoint());

        // Verify Workflow
        var wfOpt = workflowRepository.findByComplaintComplaintId(complaintId);
        assertTrue(wfOpt.isPresent());
        assertEquals(ComplaintState.SUBMITTED, wfOpt.get().getCurrentState());

        // Verify Evidence
        var evList = evidenceRepository.findByComplaintComplaintIdOrderByCreatedAtAsc(complaintId);
        assertEquals(1, evList.size());
        assertEquals("s3://civic-bucket/pothole1.jpg", evList.get(0).getStorageKey());
    }
}
