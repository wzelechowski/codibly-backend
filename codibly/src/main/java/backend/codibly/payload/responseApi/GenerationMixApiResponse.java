package backend.codibly.payload.responseApi;

import java.math.BigDecimal;

public record GenerationMixApiResponse(
        String fuel,
        BigDecimal perc
) {
}