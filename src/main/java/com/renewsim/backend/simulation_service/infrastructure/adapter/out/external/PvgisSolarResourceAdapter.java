package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.shared.exception.BadRequestException;
import com.renewsim.backend.simulation_service.create.application.port.out.PvgisSolarResourcePort;
import com.renewsim.backend.simulation_service.infrastructure.config.PvgisProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Adapter for retrieving solar resource data from PVGIS.
 *
 * <p>Unlike location lookup, this integration must not fabricate fallback data
 * because PVGIS output directly affects simulation results. Failures therefore
 * stay explicit and controlled instead of degrading to synthetic profiles.</p>
 */
@Component
public class PvgisSolarResourceAdapter implements PvgisSolarResourcePort {

    private static final Logger log = LoggerFactory.getLogger(PvgisSolarResourceAdapter.class);

    private final PvgisProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public PvgisSolarResourceAdapter(
            PvgisProperties properties,
            ObjectMapper objectMapper,
            @Qualifier("pvgisRestTemplate") RestTemplate restTemplate,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("simulationPvgis");
        this.retry = retryRegistry.retry("simulationPvgis");
    }

    PvgisSolarResourceAdapter(PvgisProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this(
                properties,
                objectMapper,
                restTemplate,
                CircuitBreakerRegistry.ofDefaults(),
                RetryRegistry.ofDefaults());
    }

    @Override
    public PvgisSolarResourceProfile fetchProfile(double latitude, double longitude, double systemLossPct) {
        try {
            return execute(() -> doFetchProfile(latitude, longitude, systemLossPct));
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error fetching PVGIS profile lat={} lon={} lossPct={} reason={}",
                    latitude,
                    longitude,
                    systemLossPct,
                    summarizeFailure(ex));
            throw new IllegalStateException("Failed to fetch PVGIS solar resource data", ex);
        }
    }

    private PvgisSolarResourceProfile doFetchProfile(double latitude, double longitude, double systemLossPct) {
        try {
            JsonNode pvcalc = objectMapper
                    .readTree(restTemplate.getForObject(pvcalcUrl(latitude, longitude, systemLossPct), String.class));
            JsonNode tmy = objectMapper.readTree(restTemplate.getForObject(tmyUrl(latitude, longitude), String.class));

            List<Double> monthlyGeneration = readMonthlyValues(pvcalc.path("outputs").path("monthly").path("fixed"),
                    "E_m");
            List<Double> monthlyIrradiance = readMonthlyValues(pvcalc.path("outputs").path("monthly").path("fixed"),
                    "H(i)_m");
            List<Double> monthlyTemperature = readMonthlyTemperatures(tmy.path("outputs").path("tmy_hourly"));

            if (monthlyGeneration.size() != 12 || monthlyIrradiance.size() != 12 || monthlyTemperature.size() != 12) {
                throw new BadRequestException("PVGIS response is incomplete for monthly solar resource data");
            }

            JsonNode meteoData = pvcalc.path("inputs").path("meteo_data");
            String climatePeriod = meteoData.path("year_min").asText() + "-" + meteoData.path("year_max").asText();

            return new PvgisSolarResourceProfile(monthlyGeneration, monthlyIrradiance, monthlyTemperature,
                    climatePeriod, "PVGIS");
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse PVGIS solar resource data", ex);
        }
    }

    private <T> T execute(Supplier<T> supplier) {
        // Use explicit registries so resilience is exercised consistently even in direct adapter tests.
        Supplier<T> decorated = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        decorated = Retry.decorateSupplier(retry, decorated);
        return decorated.get();
    }

    private String pvcalcUrl(double latitude, double longitude, double systemLossPct) {
        return UriComponentsBuilder.fromUriString(properties.url() + "/api/v5_2/PVcalc")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("peakpower", 1)
                .queryParam("loss", systemLossPct)
                .queryParam("optimalangles", 1)
                .queryParam("outputformat", "json")
                .toUriString();
    }

    private String tmyUrl(double latitude, double longitude) {
        return UriComponentsBuilder.fromUriString(properties.url() + "/api/v5_2/tmy")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("outputformat", "json")
                .toUriString();
    }

    private List<Double> readMonthlyValues(JsonNode monthlyNodes, String fieldName) {
        List<Double> values = new ArrayList<>();
        for (JsonNode monthlyNode : monthlyNodes) {
            values.add(monthlyNode.path(fieldName).asDouble());
        }
        return values;
    }

    private List<Double> readMonthlyTemperatures(JsonNode hourlyNodes) {
        double[] sums = new double[12];
        int[] counts = new int[12];
        for (JsonNode hourlyNode : hourlyNodes) {
            String timestamp = hourlyNode.path("time(UTC)").asText();
            if (timestamp.length() < 6) {
                continue;
            }
            int monthIndex = Integer.parseInt(timestamp.substring(4, 6)) - 1;
            if (monthIndex < 0 || monthIndex > 11) {
                continue;
            }
            sums[monthIndex] += hourlyNode.path("T2m").asDouble();
            counts[monthIndex]++;
        }

        List<Double> temperatures = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            temperatures.add(counts[i] == 0 ? 0.0 : sums[i] / counts[i]);
        }
        return temperatures;
    }

    private String summarizeFailure(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return throwable.getClass().getSimpleName() + ": " + message.replaceAll("[\r\n]", " ");
    }
}
