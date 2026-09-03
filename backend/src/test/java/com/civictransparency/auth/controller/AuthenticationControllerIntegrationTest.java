package com.civictransparency.auth.controller;

import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.citizen.repository.CitizenProfileRepository;
import com.civictransparency.identity.entity.Role;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.entity.UserRole;
import com.civictransparency.identity.repository.RoleRepository;
import com.civictransparency.identity.repository.UserAccountRepository;
import com.civictransparency.identity.repository.UserRoleRepository;
import com.civictransparency.security.PasswordHashingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserAccountRepository userAccountRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private CitizenProfileRepository citizenProfileRepository;
    @Autowired private PasswordHashingService passwordHashingService;

    @Test
    void shouldLoginWithEmailAndIssueBearerToken() throws Exception {
        String email = "login." + UUID.randomUUID() + "@example.com";
        UserAccount user = createCitizen(email, "loginuser", "TestPassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"TestPassword123!"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(user.getUserId().toString()))
                .andExpect(jsonPath("$.publicAlias").value("Login Test Citizen"))
                .andExpect(jsonPath("$.roles[0]").value("CITIZEN"));

        assertNotNull(userAccountRepository.findById(user.getUserId()).orElseThrow().getLastLoginAt());
    }

    @Test
    void shouldLoginWithUsername() throws Exception {
        createCitizen("username." + UUID.randomUUID() + "@example.com", "usernameuser", "TestPassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"identifier\":\"usernameuser\",\"password\":\"TestPassword123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("usernameuser"));
    }

    @Test
    void shouldRejectIncorrectPasswordWithoutDisclosingAccountState() throws Exception {
        String email = "incorrect." + UUID.randomUUID() + "@example.com";
        createCitizen(email, "incorrectuser", "TestPassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"%s","password":"incorrect"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    private UserAccount createCitizen(String email, String username, String password) {
        UserAccount user = new UserAccount(email, "ACTIVE");
        user.setUsername(username);
        user.setPasswordHash(passwordHashingService.hash(password));
        user = userAccountRepository.save(user);

        Role citizenRole = roleRepository.findByCode("CITIZEN").orElseThrow();
        userRoleRepository.save(new UserRole(user, citizenRole));

        CitizenProfile profile = new CitizenProfile(user, "Login Test Citizen", "ACTIVE");
        citizenProfileRepository.save(profile);
        return user;
    }
}
