package backend.codibly.controller;

import backend.codibly.payload.response.GenerationMixResponse;
import backend.codibly.payload.response.OptimalChargingWindowResponse;
import backend.codibly.service.GenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/generation")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GenerationController {

    private final GenerationService generationService;

    @GetMapping
    ResponseEntity<List<GenerationMixResponse>> getGenerationMix() {
        return ResponseEntity.ok(generationService.getGenerationMix());
    }

    @GetMapping("/window/{hours}")
    ResponseEntity<OptimalChargingWindowResponse> getOptimalCharging(@PathVariable int hours) {
        return ResponseEntity.ok(generationService.getOptimalChargingWindow(hours));
    }
}
