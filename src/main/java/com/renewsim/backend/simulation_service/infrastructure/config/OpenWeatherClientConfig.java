package com.renewsim.backend.simulation_service.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(WeatherServiceProperties.class)
/**
 * Configures the dedicated HTTP client used by the OpenWeather adapter.
 *
 * <p>Keeps timeout policy centralized and separate from adapter logic.</p>
 */
public class OpenWeatherClientConfig {

    /**
     * Creates a RestTemplate with explicit connect and read timeouts for
     * outbound OpenWeather calls.
     */
    @Bean("openWeatherRestTemplate")
    RestTemplate openWeatherRestTemplate(WeatherServiceProperties weatherProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) weatherProperties.connectTimeout().toMillis());
        requestFactory.setReadTimeout((int) weatherProperties.readTimeout().toMillis());
        return new RestTemplate(requestFactory);
    }
}
