package com.renewsim.backend.simulation_service.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configures the dedicated HTTP client used by the PVGIS adapter.
 *
 * <p>This keeps transport concerns, especially timeout policy, outside the
 * adapter so the adapter focuses on provider translation and failure handling.</p>
 */
@Configuration
@EnableConfigurationProperties(PvgisProperties.class)
public class PvgisClientConfig {

    @Bean("pvgisRestTemplate")
    RestTemplate pvgisRestTemplate(PvgisProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        requestFactory.setReadTimeout((int) properties.readTimeout().toMillis());
        return new RestTemplate(requestFactory);
    }
}
