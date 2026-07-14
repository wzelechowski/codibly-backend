package backend.codibly.payload.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record OptimalChargingWindowResponse(
        ZonedDateTime from,
        ZonedDateTime to,
        BigDecimal ecoPercentage
) {
}
