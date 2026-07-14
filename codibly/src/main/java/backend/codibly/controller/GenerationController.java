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
@CrossOrigin(origins = "${app.frontend.url}", maxAge = 3600)
public class GenerationController {

    private final GenerationService generationService;

    @GetMapping
    ResponseEntity<List<GenerationMixResponse>> getGenerationMix() {
        return ResponseEntity.ok(generationService.getGenerationMix());
    }

    @GetMapping("/window")
    ResponseEntity<OptimalChargingWindowResponse> getOptimalCharging(@RequestParam int hours) {
        return ResponseEntity.ok(generationService.getOptimalChargingWindow(hours));
    }
}
