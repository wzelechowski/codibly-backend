package backend.codibly.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 8089)
@TestPropertySource(properties = {
        "carbon.intensity.api.url=http://localhost:8089/generation"
})
class GenerationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private void stubCarbonIntensityApi() {
        String mockJsonResponse = """
                {
                  "data": [
                    {
                      "from": "2026-01-01T00:00Z",
                      "to": "2026-01-01T00:30Z",
                      "generationmix": [
                        {"fuel": "biomass", "perc": 10.0},
                        {"fuel": "wind", "perc": 40.0}
                      ]
                    },
                    {
                      "from": "2026-01-01T00:30Z",
                      "to": "2026-01-01T01:00Z",
                      "generationmix": [
                        {"fuel": "biomass", "perc": 15.0},
                        {"fuel": "wind", "perc": 45.0}
                      ]
                    },
                    {
                      "from": "2026-01-01T01:00Z",
                      "to": "2026-01-01T01:30Z",
                      "generationmix": [
                        {"fuel": "biomass", "perc": 20.0},
                        {"fuel": "wind", "perc": 50.0}
                      ]
                    },
                    {
                      "from": "2026-01-01T01:30Z",
                      "to": "2026-01-01T02:00Z",
                      "generationmix": [
                        {"fuel": "biomass", "perc": 25.0},
                        {"fuel": "wind", "perc": 55.0}
                      ]
                    }
                  ]
                }
                """;

        stubFor(WireMock.get(urlPathMatching("/generation/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(mockJsonResponse)));
    }

    @Test
    void shouldReturn200WhenCallingGeneration() throws Exception {
        stubCarbonIntensityApi();

        mockMvc.perform(get("/generation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))))
                .andExpect(jsonPath("$[0].date").exists())
                .andExpect(jsonPath("$[0].ecoPercentage").value(65.0));
    }

    @Test
    void shouldReturn200WhenCallingOptimizeChargingWindow() throws Exception {
        stubCarbonIntensityApi();

        mockMvc.perform(get("/generation/window")
                        .param("hours", "1")
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