package backend.codibly.payload.responseApi;

import java.util.List;

public record GenerationDataApiResponse(
        String from,
        String to,
        List<GenerationMixApiResponse> generationmix
) {
}