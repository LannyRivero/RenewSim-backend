package com.renewsim.backend.simulation_service.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds outbound PVGIS configuration for the simulation module.
 *
 * <p>Keeps the provider base URL and timeout policy explicit so the external
 * integration can be tuned operationally without changing adapter code.</p>
 */
@ConfigurationProperties(prefix = "services.pvgis")
public record PvgisProperties(String url, Duration connectTimeout, Duration readTimeout) {

    public PvgisProperties {
        url = (url == null || url.isBlank()) ? "https://re.jrc.ec.europa.eu" : url;
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(2) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(5) : readTimeout;
    }
}
