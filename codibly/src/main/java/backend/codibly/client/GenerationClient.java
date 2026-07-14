package backend.codibly.client;

import backend.codibly.payload.responseApi.GenerationApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class GenerationClient {

    private final RestClient restClient;
    @Value("${carbon.intensity.api.url}")
    private String intensityUrl;

    public GenerationApiResponse fetchGenerations(ZonedDateTime from, ZonedDateTime to) {
        String formattedFrom = from.format(DateTimeFormatter.ISO_INSTANT);
        String formattedTo = to.format(DateTimeFormatter.ISO_INSTANT);
        String targetUrl = intensityUrl + "/" + formattedFrom + "/" + formattedTo;
        return restClient.get()
                .uri(URI.create(targetUrl))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    String errorBody = new String(res.getBody().readAllBytes());
                    System.err.println("Błąd z API Carbon Intensity: " + errorBody);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "External API Error: " + errorBody);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Service is unavailable");
                })
                .body(GenerationApiResponse.class);
    }
}
