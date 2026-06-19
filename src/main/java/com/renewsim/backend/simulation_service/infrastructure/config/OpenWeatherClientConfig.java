package com.renewsim.backend.simulation_service.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(WeatherServiceProperties.class)
public class OpenWeatherClientConfig {

    @Bean("openWeatherRestTemplate")
    RestTemplate openWeatherRestTemplate(WeatherServiceProperties weatherProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) weatherProperties.connectTimeout().toMillis());
        requestFactory.setReadTimeout((int) weatherProperties.readTimeout().toMillis());
        return new RestTemplate(requestFactory);
    }
}
