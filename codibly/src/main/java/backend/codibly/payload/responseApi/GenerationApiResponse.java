package backend.codibly.payload.responseApi;

import java.util.List;

public record GenerationApiResponse(
        List<GenerationDataApiResponse> data
) {
}
