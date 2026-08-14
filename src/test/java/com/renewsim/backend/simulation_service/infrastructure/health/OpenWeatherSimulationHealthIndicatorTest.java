package com.renewsim.backend.simulation_service.infrastructure.health;

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

class OpenWeatherSimulationHealthIndicatorTest {

    @Test
    @DisplayName("health is unknown when openweather provider is not active")
    void healthIsUnknownWhenProviderIsNotActive() {
        OpenWeatherSimulationHealthIndicator indicator = new OpenWeatherSimulationHealthIndicator(
                new SimulationClimateProperties("dummy"),
                new WeatherServiceProperties("https://api.openweathermap.org", "dummy-key", Duration.ofSeconds(2), Duration.ofSeconds(3)),
                providerOf(null),
                new MockEnvironment().withProperty("spring.profiles.active", "local"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(health.getDetails()).containsEntry("reason", "provider_not_active");
    }

    @Test
    @DisplayName("health is down when active openweather provider has no lookup bean")
    void healthIsDownWhenLookupBeanIsMissing() {
        OpenWeatherSimulationHealthIndicator indicator = new OpenWeatherSimulationHealthIndicator(
                new SimulationClimateProperties("openweathermap"),
                new WeatherServiceProperties("https://api.openweathermap.org", "real-key", Duration.ofSeconds(2), Duration.ofSeconds(3)),
                providerOf(null),
                new MockEnvironment().withProperty("spring.profiles.active", "local"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "missing_location_lookup_provider");
    }

    private ObjectProvider<LocationLookupProvider> providerOf(LocationLookupProvider provider) {
        return new ObjectProvider<>() {
            @Override public LocationLookupProvider getObject(Object... args) { return provider; }
            @Override public LocationLookupProvider getIfAvailable() { return provider; }
            @Override public LocationLookupProvider getIfUnique() { return provider; }
            @Override public LocationLookupProvider getObject() { return provider; }
        };
    }
}
