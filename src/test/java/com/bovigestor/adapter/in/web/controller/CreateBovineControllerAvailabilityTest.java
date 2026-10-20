package com.bovigestor.adapter.in.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = CreateBovineController.class,
        properties = "bovigestor.bovine.creation.enabled=true"
)
@ActiveProfiles("prod")
class CreateBovineControllerAvailabilityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotExposeBovineCreationOutsideDevelopmentEvenWhenFeatureFlagIsForced() throws Exception {
        mockMvc.perform(post("/api/v1/bovines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}
