package com.renewsim.backend.simulation_service.infrastructure.health;

import com.renewsim.backend.simulation_service.infrastructure.config.PvgisProperties;
import com.renewsim.backend.simulation_service.infrastructure.config.SimulationClimateProperties;
import com.renewsim.backend.simulation_service.infrastructure.config.WeatherServiceProperties;
import com.renewsim.backend.simulation_service.location_lookup.application.port.out.LocationLookupProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SimulationServiceHealthIndicatorTest {

    @Test
    @DisplayName("health is up for valid openweather configuration")
    void healthIsUpForValidOpenWeatherConfiguration() {
        SimulationServiceHealthIndicator indicator = new SimulationServiceHealthIndicator(
                new SimulationClimateProperties("openweathermap"),
                new WeatherServiceProperties("https://api.openweathermap.org", "real-key", Duration.ofSeconds(2),
                        Duration.ofSeconds(3)),
                new PvgisProperties("https://re.jrc.ec.europa.eu", null, null),
                providerOf(mock(LocationLookupProvider.class)),
                new MockEnvironment().withProperty("spring.profiles.active", "stage"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("climateProvider", "openweathermap");
    }

    @Test
    @DisplayName("health is down when openweather provider is configured without lookup bean")
    void healthIsDownWhenOpenWeatherBeanIsMissing() {
        SimulationServiceHealthIndicator indicator = new SimulationServiceHealthIndicator(
                new SimulationClimateProperties("openweathermap"),
                new WeatherServiceProperties("https://api.openweathermap.org", "real-key", Duration.ofSeconds(2),
                        Duration.ofSeconds(3)),
                new PvgisProperties("https://re.jrc.ec.europa.eu", null, null),
                providerOf(null),
                new MockEnvironment().withProperty("spring.profiles.active", "local"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "missing_location_lookup_provider");
    }

    @Test
    @DisplayName("health is down for invalid pvgis url")
    void healthIsDownForInvalidPvgisUrl() {
        SimulationServiceHealthIndicator indicator = new SimulationServiceHealthIndicator(
                new SimulationClimateProperties("dummy"),
                new WeatherServiceProperties("https://api.openweathermap.org", "dummy-key", Duration.ofSeconds(2),
                        Duration.ofSeconds(3)),
                new PvgisProperties("not-a-valid-url", null, null),
                providerOf(null),
                new MockEnvironment().withProperty("spring.profiles.active", "local"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "invalid_pvgis_url");
    }

    private ObjectProvider<LocationLookupProvider> providerOf(LocationLookupProvider provider) {
        return new ObjectProvider<>() {
            @Override
            public LocationLookupProvider getObject(Object... args) {
                return provider;
            }

            @Override
            public LocationLookupProvider getIfAvailable() {
                return provider;
            }

            @Override
            public LocationLookupProvider getIfUnique() {
                return provider;
            }

            @Override
            public LocationLookupProvider getObject() {
                return provider;
            }
        };
    }
}
