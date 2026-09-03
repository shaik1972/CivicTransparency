package com.civictransparency.complaint.controller;

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
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCategories_returnsAllSixActiveCategories() throws Exception {
        mockMvc.perform(get("/api/v1/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].code", is("ROADS")))
                .andExpect(jsonPath("$[0].subcategories", hasSize(3)))
                .andExpect(jsonPath("$[1].code", is("SANITATION")))
                .andExpect(jsonPath("$[1].subcategories", hasSize(3)))
                .andExpect(jsonPath("$[2].code", is("WATER")))
                .andExpect(jsonPath("$[2].subcategories", hasSize(3)))
                .andExpect(jsonPath("$[3].code", is("SAFETY")))
                .andExpect(jsonPath("$[3].subcategories", hasSize(3)))
                .andExpect(jsonPath("$[4].code", is("SCHOOL")))
                .andExpect(jsonPath("$[4].subcategories", hasSize(2)))
                .andExpect(jsonPath("$[5].code", is("FOOD")))
                .andExpect(jsonPath("$[5].subcategories", hasSize(2)));
    }

    @Test
    void getCategories_eachSubcategoryHasDefaultPriority() throws Exception {
        mockMvc.perform(get("/api/v1/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Subcategories are alphabetically ordered; verify at least one HIGH-priority subcategory
                // exists within ROADS (e.g. "Pothole / Damaged Road Surface")
                .andExpect(jsonPath("$[0].subcategories[*].defaultPriority", hasItem("HIGH")))
                .andExpect(jsonPath("$[0].subcategories[*].defaultPriority", hasItem("MEDIUM")));
    }
}
