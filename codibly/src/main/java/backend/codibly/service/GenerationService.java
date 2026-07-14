package backend.codibly.service;

import backend.codibly.payload.response.GenerationMixResponse;
import backend.codibly.payload.response.OptimalChargingWindowResponse;

import java.util.List;

public interface GenerationService {
    List<GenerationMixResponse> getGenerationMix();
    OptimalChargingWindowResponse getOptimalChargingWindow(int hours);
}
