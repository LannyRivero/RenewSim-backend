package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import com.renewsim.backend.simulation_service.application.port.out.LocationLookupPort;
import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import com.renewsim.backend.simulation_service.infrastructure.config.WeatherServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Adapter for resolving and searching locations through OpenWeatherMap.
 *
 * <p>Implements {@link LocationLookupPort} and is activated only when the
 * simulation climate provider is configured as {@code openweathermap}.</p>
 */
@Component
@ConditionalOnProperty(prefix = "simulation.climate", name = "provider", havingValue = "openweathermap")
@RequiredArgsConstructor
public class OpenWeatherMapAdapter implements LocationLookupPort {

    private static final Logger log = LoggerFactory.getLogger(OpenWeatherMapAdapter.class);

    private final WeatherServiceProperties weatherProperties;
    @Qualifier("openWeatherRestTemplate")
    private final RestTemplate restTemplate;

    @Override
    public ResolvedLocation resolveLocation(double latitude, double longitude) {
        try {
            Map<?, ?> response = fetchWeatherResponse(latitude, longitude);
            if (response == null) {
                return new ResolvedLocation(String.format("%.4f, %.4f", latitude, longitude), "Unknown");
            }

            String name = response.get("name") != null ? String.valueOf(response.get("name")) : null;
            Map<?, ?> sys = (Map<?, ?>) response.get("sys");
            String country = sys != null && sys.get("country") != null ? String.valueOf(sys.get("country")) : null;

            return new ResolvedLocation(
                    name != null && !name.isBlank() ? name : String.format("%.4f, %.4f", latitude, longitude),
                    country != null && !country.isBlank() ? country : "Unknown");
        } catch (Exception e) {
            log.error("Error resolving location for coordinates: {}, {} ({})",
                    latitude,
                    longitude,
                    summarizeFailure(e));
            return new ResolvedLocation(String.format("%.4f, %.4f", latitude, longitude), "Unknown");
        }
    }

    @Override
    public List<ResolvedLocation> searchLocations(String query, int limit) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl() + "/geo/1.0/direct")
                    .queryParam("q", query)
                    .queryParam("limit", Math.max(1, limit))
                    .queryParam("appid", apiKey())
                    .toUriString();

            List<?> response = restTemplate.getForObject(url, List.class);
            if (response == null) {
                return List.of();
            }

            return response.stream()
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast)
                    .map(this::toResolvedLocation)
                    .toList();
        } catch (Exception e) {
            log.error("Error searching locations for query: {} ({})", query, summarizeFailure(e));
            return List.of();
        }
    }

    private ResolvedLocation toResolvedLocation(Map<?, ?> candidate) {
        String name = candidate.get("name") != null ? String.valueOf(candidate.get("name")) : "Unknown";
        String state = candidate.get("state") != null ? String.valueOf(candidate.get("state")) : null;
        String country = candidate.get("country") != null ? String.valueOf(candidate.get("country")) : "Unknown";
        double latitude = candidate.get("lat") instanceof Number lat ? lat.doubleValue() : 0.0;
        double longitude = candidate.get("lon") instanceof Number lon ? lon.doubleValue() : 0.0;

        String resolvedName = state != null && !state.isBlank()
                ? String.format("%s, %s", name, state)
                : name;

        return new ResolvedLocation(resolvedName, country, latitude, longitude);
    }

    private Map<?, ?> fetchWeatherResponse(double latitude, double longitude) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl() + "/data/2.5/weather")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("units", "metric")
                .queryParam("appid", apiKey())
                .toUriString();

        return restTemplate.getForObject(url, Map.class);
    }

    private String baseUrl() {
        return weatherProperties.url();
    }

    private String apiKey() {
        return weatherProperties.key();
    }

    private String summarizeFailure(Exception exception) {
        String type = exception.getClass().getSimpleName();
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return type;
        }
        return type + ": " + sanitize(message);
    }

    private String sanitize(String value) {
        return value
                .replaceAll("appid=[^&\\s]+", "appid=<redacted>")
                .replace(apiKey(), "<redacted>");
    }

}
