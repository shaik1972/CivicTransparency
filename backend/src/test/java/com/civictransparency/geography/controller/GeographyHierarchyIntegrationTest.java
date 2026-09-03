package com.civictransparency.geography.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GeographyHierarchyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Bengaluru city UUID seeded in V5
    private static final String BENGALURU_CITY_ID = "22222222-2222-4222-8222-222222222222";
    // Koramangala ward UUID seeded in V7
    private static final String KORAMANGALA_WARD_ID = "c1000000-0000-4000-8000-000000000001";

    @Test
    void getWards_forBengaluru_returnsSixWards() throws Exception {
        mockMvc.perform(get("/api/v1/geography/cities/{cityId}/wards", BENGALURU_CITY_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[*].name", hasItem("Koramangala")))
                .andExpect(jsonPath("$[*].name", hasItem("Indiranagar")))
                .andExpect(jsonPath("$[*].name", hasItem("HSR Layout")))
                .andExpect(jsonPath("$[*].name", hasItem("Whitefield")))
                .andExpect(jsonPath("$[*].name", hasItem("Jayanagar")))
                .andExpect(jsonPath("$[*].name", hasItem("Malleshwaram")));
    }

    @Test
    void getLocalities_forKoramangala_returnsFourLocalities() throws Exception {
        mockMvc.perform(get("/api/v1/geography/wards/{wardId}/localities", KORAMANGALA_WARD_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].name", hasItem("Koramangala 1st Block")))
                .andExpect(jsonPath("$[*].name", hasItem("80ft Road")));
    }

    @Test
    void getWards_returnsCorrectCityId() throws Exception {
        mockMvc.perform(get("/api/v1/geography/cities/{cityId}/wards", BENGALURU_CITY_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cityId", is(BENGALURU_CITY_ID)));
    }
}
