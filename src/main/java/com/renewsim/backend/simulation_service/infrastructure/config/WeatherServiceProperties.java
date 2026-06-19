package com.renewsim.backend.simulation_service.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds outbound weather-service configuration for the simulation module.
 *
 * <p>Includes the OpenWeather base URL, API key, and client timeout policy
 * used by the dedicated HTTP client.</p>
 */
@ConfigurationProperties(prefix = "services.weather")
public record WeatherServiceProperties(String url, String key, Duration connectTimeout, Duration readTimeout) {

    public WeatherServiceProperties {
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(2) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(3) : readTimeout;
    }
}
