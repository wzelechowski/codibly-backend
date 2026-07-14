package backend.codibly.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GenerationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn200WhenCallingGeneration() throws Exception {
        mockMvc.perform(get("/generation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$[0].date").exists())
                .andExpect(jsonPath("$[0].ecoPercentage").exists());
    }

    @Test
    void shouldReturn200WhenCallingOptimizeChargingWindow() throws Exception {
        mockMvc.perform(get("/generation/window")
                        .param("hours", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").exists())
                .andExpect(jsonPath("$.to").exists())
                .andExpect(jsonPath("$.ecoPercentage").exists());
    }

    @Test
    void shouldReturn400WhenCallingOptimizeChargingWindowWithInvalidParamValue() throws Exception {
        mockMvc.perform(get("/generation/window")
                        .param("hours", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenHoursParamIsMissing() throws Exception {
        mockMvc.perform(get("/generation/window"))
                .andExpect(status().isBadRequest());
    }
}