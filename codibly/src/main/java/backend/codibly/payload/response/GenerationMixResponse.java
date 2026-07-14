package backend.codibly.payload.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record GenerationMixResponse(
        LocalDate date,
        Map<String, BigDecimal> avgValues,
        BigDecimal ecoPercentage
) {
}
