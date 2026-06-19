package com.renewsim.backend.simulation_service.infrastructure.config;

import java.net.URI;
import java.util.Set;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClimateConfigurationValidator {

    private static final Set<String> STRICT_PROFILES = Set.of("stage", "prod");
    private static final Set<String> ALLOWED_OPENWEATHER_HOSTS = Set.of("api.openweathermap.org");

    private final SimulationClimateProperties climateProperties;
    private final WeatherServiceProperties weatherProperties;
    private final Environment environment;

    @PostConstruct
    void validate() {
        String provider = normalize(climateProperties.provider());

        if (!STRICT_PROFILES.stream().anyMatch(profile -> environment.acceptsProfiles(Profiles.of(profile)))) {
            return;
        }

        if (provider.isBlank() || provider.equals("dummy")) {
            throw new IllegalStateException("Stage/prod require a real simulation climate provider; dummy is not allowed.");
        }

        if (provider.equals("openweathermap")) {
            if (isMissingOrDummy(weatherProperties.key())) {
                throw new IllegalStateException("Stage/prod require OPENWEATHER_API_KEY when simulation.climate.provider=openweathermap.");
            }
            validateOpenWeatherBaseUrl(weatherProperties.url());
        }
    }

    private void validateOpenWeatherBaseUrl(String baseUrl) {
        URI uri;
        try {
            uri = URI.create(baseUrl);
        } catch (Exception ex) {
            throw new IllegalStateException("Stage/prod require a valid services.weather.url for OpenWeatherMap.", ex);
        }

        String scheme = normalize(uri.getScheme());
        String host = normalize(uri.getHost());

        if (!scheme.equals("https")) {
            throw new IllegalStateException("Stage/prod require services.weather.url to use HTTPS for OpenWeatherMap.");
        }

        if (!ALLOWED_OPENWEATHER_HOSTS.contains(host)) {
            throw new IllegalStateException("Stage/prod require services.weather.url to point to an approved OpenWeatherMap host.");
        }
    }

    private boolean isMissingOrDummy(String key) {
        String normalized = normalize(key);
        return normalized.isBlank() || normalized.equals("dummy-key");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
