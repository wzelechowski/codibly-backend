package backend.codibly.service;

import backend.codibly.client.GenerationClient;
import backend.codibly.payload.response.OptimalChargingWindowResponse;
import backend.codibly.payload.responseApi.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerationServiceImplTest {

    @Mock
    private GenerationClient generationClient;

    @InjectMocks
    private GenerationServiceImpl generationService;

    private GenerationApiResponse mockApiResponse;

    @BeforeEach
    void setUp() {
        GenerationMixApiResponse mix = new GenerationMixApiResponse("wind", BigDecimal.valueOf(50.0));

        List<GenerationDataApiResponse> dataList = List.of(
                new GenerationDataApiResponse(ZonedDateTime.now().toString(), ZonedDateTime.now().plusHours(1).toString(), List.of(mix)),
                new GenerationDataApiResponse(ZonedDateTime.now().plusHours(1).toString(), ZonedDateTime.now().plusHours(2).toString(), List.of(mix)),
                new GenerationDataApiResponse(ZonedDateTime.now().plusHours(2).toString(), ZonedDateTime.now().plusHours(3).toString(), List.of(mix))
        );

        mockApiResponse = new GenerationApiResponse(dataList);
    }

    @Test
    void shouldReturnGenerationMixSuccessfully() {
        when(generationClient.fetchGenerations(any(), any())).thenReturn(mockApiResponse);

        var result = generationService.getGenerationMix();

        assertFalse(result.isEmpty());
        assertEquals(BigDecimal.valueOf(50.0).setScale(2), result.get(0).ecoPercentage());
    }

    @Test
    void shouldCalculateOptimalWindow() {
        when(generationClient.fetchGenerations(any(), any())).thenReturn(mockApiResponse);

        OptimalChargingWindowResponse result = generationService.getOptimalChargingWindow(1);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(50.0).setScale(2), result.ecoPercentage());
    }

    @Test
    void shouldThrowBadRequestForInvalidHours() {
        assertThrows(ResponseStatusException.class, () -> generationService.getOptimalChargingWindow(7));
    }
}