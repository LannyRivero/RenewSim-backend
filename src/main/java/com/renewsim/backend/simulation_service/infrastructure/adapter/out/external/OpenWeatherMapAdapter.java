package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 🌤 Adapter for fetching weather data from OpenWeatherMap API.
 *
 * ✅ Implements ClimateDataProviderPort.
 * ✅ Optional component, can be disabled if frontend provides the data.
 */
//@Component
@Profile("weather-enabled") 
@RequiredArgsConstructor
public class OpenWeatherMapAdapter implements ClimateDataProviderPort {

    private static final Logger log = LoggerFactory.getLogger(OpenWeatherMapAdapter.class);

    @Value("${services.weather.url}")
    private String baseUrl;

    @Value("${services.weather.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ClimateData fetchClimateData(String location) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/data/2.5/weather")
                    .queryParam("q", location)
                    .queryParam("units", "metric")
                    .queryParam("appid", apiKey)
                    .toUriString();

            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response == null) return defaultClimate();

            Map<?, ?> main = (Map<?, ?>) response.get("main");
            Map<?, ?> wind = (Map<?, ?>) response.get("wind");

            double temperature = main != null && main.get("temp") != null ? ((Number) main.get("temp")).doubleValue() : 0.0;
            double windSpeed = wind != null && wind.get("speed") != null ? ((Number) wind.get("speed")).doubleValue() : 0.0;

            double irradiance = Math.max(100, 1000 - (temperature * 12));
            double hydrology = Math.max(0, windSpeed * 2);

            return new ClimateData(irradiance, windSpeed, hydrology);

        } catch (Exception e) {
            log.error("Error fetching weather data for location: {}", location, e);
            return defaultClimate();
        }
    }

    private ClimateData defaultClimate() {
        return new ClimateData(800, 5, 1);
    }
}
