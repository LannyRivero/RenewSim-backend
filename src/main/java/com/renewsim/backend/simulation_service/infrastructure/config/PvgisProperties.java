package com.renewsim.backend.simulation_service.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.pvgis")
public record PvgisProperties(String url) {

    public PvgisProperties {
        url = (url == null || url.isBlank()) ? "https://re.jrc.ec.europa.eu" : url;
    }
}
