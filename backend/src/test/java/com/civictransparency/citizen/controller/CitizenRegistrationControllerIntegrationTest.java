package com.civictransparency.citizen.controller;

import com.civictransparency.citizen.entity.CitizenAddress;
import com.civictransparency.citizen.entity.CitizenContact;
import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.citizen.repository.CitizenAddressRepository;
import com.civictransparency.citizen.repository.CitizenContactRepository;
import com.civictransparency.citizen.repository.CitizenProfileRepository;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.repository.UserAccountRepository;
import com.civictransparency.identity.repository.UserRoleRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CitizenRegistrationControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserAccountRepository userAccountRepository;

        @Autowired
        private UserRoleRepository userRoleRepository;

        @Autowired
        private CitizenProfileRepository citizenProfileRepository;

        @Autowired
        private CitizenContactRepository citizenContactRepository;

        @Autowired
        private CitizenAddressRepository citizenAddressRepository;

        @Test
        void shouldRegisterCitizenSuccessfully() throws Exception {

                String email = "v3.success." + UUID.randomUUID()
                                + "@example.com";

                String username = "v3success" + UUID.randomUUID()
                                .toString()
                                .substring(0, 8);

                String request = """
                                {
                                    "email": "%s",
                                    "username": "%s",
                                    "password": "TestPassword123!",
                                    "confirmPassword": "TestPassword123!",
                                    "publicAlias": "V3 Test Citizen",
                                    "phone": "9876543210",
                                    "address": "123 V3 Test Street, Bengaluru",
                                    "cityId": null,
                                    "postalCode": "560001"
                                }
                                """.formatted(email, username);

                mockMvc.perform(
                                post("/api/v1/citizens/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(request))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.citizenId").exists())
                                .andExpect(jsonPath("$.userId").exists())
                                .andExpect(jsonPath("$.username").value(username))
                                .andExpect(jsonPath("$.publicAlias")
                                                .value("V3 Test Citizen"))
                                .andExpect(jsonPath("$.profileStatus")
                                                .value("ACTIVE"))
                                .andExpect(jsonPath("$.userStatus")
                                                .value("ACTIVE"))
                                .andExpect(jsonPath("$.roleCode")
                                                .value("CITIZEN"));

                UserAccount user = userAccountRepository
                                .findByEmail(email)
                                .orElseThrow();

                var userRoles = userRoleRepository.findByIdUserId(user.getUserId());

                assertEquals(1, userRoles.size());

                assertEquals(
                                "CITIZEN",
                                userRoles.get(0).getRole().getCode());

                assertNotEquals(
                                "TestPassword123!",
                                user.getPasswordHash(),
                                "Password must never be stored as plaintext");

                CitizenProfile profile = citizenProfileRepository
                                .findByUserUserId(user.getUserId())
                                .orElseThrow();

                assertEquals(
                                "V3 Test Citizen",
                                profile.getPublicAlias());

                CitizenContact contact = citizenContactRepository
                                .findById(profile.getCitizenId())
                                .orElseThrow();

                assertNotEquals(
                                "9876543210",
                                contact.getPhoneEncrypted(),
                                "Phone must be encrypted");

                assertNotEquals(
                                email,
                                contact.getEmailEncrypted(),
                                "Email must be encrypted");

                var addresses = citizenAddressRepository
                                .findByCitizenProfileCitizenId(
                                                profile.getCitizenId());

                assertEquals(1, addresses.size());

                CitizenAddress address = addresses.get(0);

                assertNotEquals(
                                "123 V3 Test Street, Bengaluru",
                                address.getAddressTextEncrypted(),
                                "Address must be encrypted");

                assertEquals("560001", address.getPostalCode());
                assertTrue(address.isPrimary());
        }

        @Test
        void shouldRejectPasswordMismatch() throws Exception {

                String request = """
                                {
                                    "email": "v3.mismatch@example.com",
                                    "username": "v3mismatch",
                                    "password": "TestPassword123!",
                                    "confirmPassword": "DifferentPassword123!",
                                    "publicAlias": "V3 Test Citizen",
                                    "phone": "9876543210",
                                    "address": "123 V3 Test Street, Bengaluru",
                                    "cityId": null,
                                    "postalCode": "560001"
                                }
                                """;

                mockMvc.perform(
                                post("/api/v1/citizens/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code")
                                                .value("INVALID_REGISTRATION_DATA"))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "The registration details provided are invalid."))
                                .andExpect(jsonPath("$.path")
                                                .value("/api/v1/citizens/register"));
        }

        @Test
        void shouldRejectMissingEmail() throws Exception {

                String request = """
                                {
                                    "username": "v4noemail",
                                    "password": "TestPassword123!",
                                    "confirmPassword": "TestPassword123!",
                                    "publicAlias": "V4 Test Citizen",
                                    "phone": "9876543210",
                                    "address": "123 V4 Test Street, Bengaluru",
                                    "cityId": null,
                                    "postalCode": "560001"
                                }
                                """;

                mockMvc.perform(
                                post("/api/v1/citizens/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code")
                                                .value("INVALID_REGISTRATION_DATA"))
                                .andExpect(jsonPath("$.message")
                                                .value("The registration details provided are invalid."))
                                .andExpect(jsonPath("$.path")
                                                .value("/api/v1/citizens/register"));
        }

        @Test
        void shouldRejectInvalidEmail() throws Exception {

                String request = """
                                {
                                    "email": "not-an-email",
                                    "username": "v4invalidemail",
                                    "password": "TestPassword123!",
                                    "confirmPassword": "TestPassword123!",
                                    "publicAlias": "V4 Test Citizen",
                                    "phone": "9876543210",
                                    "address": "123 V4 Test Street, Bengaluru",
                                    "cityId": null,
                                    "postalCode": "560001"
                                }
                                """;

                mockMvc.perform(
                                post("/api/v1/citizens/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code")
                                                .value("INVALID_REGISTRATION_DATA"))
                                .andExpect(jsonPath("$.message")
                                                .value("The registration details provided are invalid."))
                                .andExpect(jsonPath("$.path")
                                                .value("/api/v1/citizens/register"));
        }

        @Test
        void shouldRejectMissingPhone() throws Exception {

                String request = """
                                {
                                    "email": "v4.nophone@example.com",
                                    "username": "v4nophone",
                                    "password": "TestPassword123!",
                                    "confirmPassword": "TestPassword123!",
                                    "publicAlias": "V4 Test Citizen",
                                    "address": "123 V4 Test Street, Bengaluru",
                                    "cityId": null,
                                    "postalCode": "560001"
                                }
                                """;

                mockMvc.perform(
                                post("/api/v1/citizens/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code")
                                                .value("INVALID_REGISTRATION_DATA"))
                                .andExpect(jsonPath("$.message")
                                                .value("The registration details provided are invalid."))
                                .andExpect(jsonPath("$.path")
                                                .value("/api/v1/citizens/register"));
        }

        @Test
        void shouldRejectMissingAddress() throws Exception {

                String request = """
                                {
                                    "email": "v4.noaddress@example.com",
                                    "username": "v4noaddress",
                                    "password": "TestPassword123!",
                                    "confirmPassword": "TestPassword123!",
                                    "publicAlias": "V4 Test Citizen",
                                    "phone": "9876543210",
                                    "cityId": null,
                                    "postalCode": "560001"
                                }
                                """;

                mockMvc.perform(
                                post("/api/v1/citizens/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code")
                                                .value("INVALID_REGISTRATION_DATA"))
                                .andExpect(jsonPath("$.message")
                                                .value("The registration details provided are invalid."))
                                .andExpect(jsonPath("$.path")
                                                .value("/api/v1/citizens/register"));
        }

        @Test
        void shouldRejectDuplicateEmail() throws Exception {

                String email = "duplicate.email." + UUID.randomUUID()
                                + "@example.com";

                String firstUsername = "duplicateemail1"
                                + UUID.randomUUID()
                                                .toString()
                                                .substring(0, 8);

                String secondUsername = "duplicateemail2"
                                + UUID.randomUUID()
                                                .toString()
                                                .substring(0, 8);

                String firstRequest = """
                                {
                                    "email": "%s",
                                    "username": "%s",
                                    "password": "TestPassword123!",
                                    "confirmPassword": "TestPassword123!",
                                    "publicAlias": "First Citizen",
                                    "phone": "9876543210",
                                    "address": "123 First Street, Bengaluru",
                                    "cityId": null,
                                    "postalCode": "560001"
                                }
                                """.formatted(email, firstUsername);

                mockMvc.perform(
                                post("/api/v1/citizens/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(firstRequest))
                                .andExpect(status().isCreated());

                String secondRequest = """
                                {
                                    "email": "%s",
                                    "username": "%s",
                                    "password": "TestPassword123!",
                                    "confirmPassword": "TestPassword123!",
                                    "publicAlias": "Second Citizen",
                                    "phone": "9876543211",
                                    "address": "456 Second Street, Bengaluru",
                                    "cityId": null,
                                    "postalCode": "560002"
                                }
                                """.formatted(email, secondUsername);

                mockMvc.perform(
                                post("/api/v1/citizens/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(secondRequest))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code")
                                                .value("DUPLICATE_REGISTRATION"))
                                .andExpect(jsonPath("$.message")
                                                .value("An account with this email already exists."))
                                .andExpect(jsonPath("$.path")
                                                .value("/api/v1/citizens/register"));
        }
}
