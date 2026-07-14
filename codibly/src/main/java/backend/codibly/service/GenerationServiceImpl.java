package backend.codibly.service;

import backend.codibly.client.GenerationClient;
import backend.codibly.payload.response.*;
import backend.codibly.payload.responseApi.GenerationApiResponse;
import backend.codibly.payload.responseApi.GenerationDataApiResponse;
import backend.codibly.payload.responseApi.GenerationMixApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerationServiceImpl implements GenerationService {

    private final GenerationClient generationClient;
    private static final Set<String> ECO_ENERGY_RESOURCES = Set.of("biomass", "nuclear", "hydro", "wind", "solar");
    private static final Collector<GenerationMixApiResponse, ?, Map<String, BigDecimal>> DAILY_FUEL_AVERAGE_COLLECTOR =
            Collectors.groupingBy(
                    GenerationMixApiResponse::fuel,
                    Collectors.collectingAndThen(
                            Collectors.averagingDouble(mix -> mix.perc().doubleValue()),
                            avg -> BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
                    )
            );

    private static final Collector<GenerationDataApiResponse, ?, Map<LocalDate, List<GenerationMixApiResponse>>> GENERATION_MIX_COLLECTOR =
            Collectors.groupingBy(
                    d -> ZonedDateTime.parse(d.from()).toLocalDate(),
                    Collectors.flatMapping(
                            d -> d.generationmix().stream(),
                            Collectors.toList()
                    )
            );

    @Override
    public List<GenerationMixResponse> getGenerationMix() {
        GenerationApiResponse fetchedData = fetchGeneration();
        Map<LocalDate, List<GenerationMixApiResponse>> fuelsByDate = fetchedData.data().stream()
                .collect(GENERATION_MIX_COLLECTOR);

        return fuelsByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(this::aggregateGenerationMix)
                .toList();
    }

    @Override
    public OptimalChargingWindowResponse getOptimalChargingWindow(int hours) {
        if (hours < 1 || hours > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid hours value");
        }

        List<GenerationDataApiResponse> fetchedData = fetchGeneration().data();
        int windowSize = 2*hours;
        BigDecimal bestAverage = BigDecimal.valueOf(-1);
        int bestStartIndex = 0;
        for (int i = 0; i <= fetchedData.size() - windowSize; i++) {
            List<GenerationDataApiResponse> currentWindow = fetchedData.subList(i, i + windowSize);
            BigDecimal currentAverage = calculateCleanEnergyAverageForChargingWindow(currentWindow);

            if (currentAverage.compareTo(bestAverage) > 0) {
                bestAverage = currentAverage;
                bestStartIndex = i;
            }
        }

        GenerationDataApiResponse bestStartElement = fetchedData.get(bestStartIndex);
        GenerationDataApiResponse bestEndElement = fetchedData.get(bestStartIndex + windowSize - 1);
        return new OptimalChargingWindowResponse(
                ZonedDateTime.parse(bestStartElement.from()),
                ZonedDateTime.parse(bestEndElement.to()),
                bestAverage
        );
    }

    private GenerationApiResponse fetchGeneration() {
        ZonedDateTime from = ZonedDateTime.now(ZoneId.of("UTC")).with(LocalTime.of(0, 1));
        ZonedDateTime to = from.plusDays(2).with(LocalTime.MAX);
        return generationClient.fetchGenerations(from, to);
    }

    private GenerationMixResponse aggregateGenerationMix(Map.Entry<LocalDate, List<GenerationMixApiResponse>> entry) {
        LocalDate date = entry.getKey();
        List<GenerationMixApiResponse> dailyFuels = entry.getValue();
        Map<String, BigDecimal> dailyAverages = dailyFuels.stream()
                .collect(DAILY_FUEL_AVERAGE_COLLECTOR);
        BigDecimal ecoPercentage = dailyAverages.entrySet().stream()
                .filter(e -> ECO_ENERGY_RESOURCES.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new GenerationMixResponse(date, dailyAverages, ecoPercentage);
    }

    private BigDecimal calculateCleanEnergyAverageForChargingWindow(List<GenerationDataApiResponse> window) {
        double totalCleanEnergyInWindow = 0.0;
        for (GenerationDataApiResponse interval : window) {
            double intervalEcoSum = 0.0;
            for (GenerationMixApiResponse mix : interval.generationmix()) {
                if (ECO_ENERGY_RESOURCES.contains(mix.fuel())) {
                    intervalEcoSum += mix.perc().doubleValue();
                }
            }
            totalCleanEnergyInWindow += intervalEcoSum;
        }

        double averageCleanEnergy = totalCleanEnergyInWindow / window.size();
        return BigDecimal.valueOf(averageCleanEnergy).setScale(2, RoundingMode.HALF_UP);
    }
}
