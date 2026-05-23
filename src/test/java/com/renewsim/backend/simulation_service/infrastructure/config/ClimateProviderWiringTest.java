package com.renewsim.backend.simulation_service.infrastructure.config;

import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.dummy.DummyClimateDataAdapter;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.external.OpenWeatherMapAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ClimateProviderWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DummyClimateDataAdapter.class,
                    OpenWeatherMapAdapter.class));

    @Test
    @DisplayName("non-weather profiles default to dummy climate provider")
    void nonWeatherProfilesDefaultToDummyClimateProvider() {
        contextRunner
                .withPropertyValues("spring.profiles.active=local")
                .run(context -> {
                    assertThat(context).hasSingleBean(ClimateDataProviderPort.class);
                    assertThat(context.getBean(ClimateDataProviderPort.class)).isInstanceOf(DummyClimateDataAdapter.class);
                    assertThat(context).doesNotHaveBean(OpenWeatherMapAdapter.class);
                });
    }

    @Test
    @DisplayName("weather-enabled profile uses OpenWeatherMap when explicitly configured")
    void weatherEnabledProfileUsesOpenWeatherMapWhenConfigured() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=weather-enabled",
                        "simulation.climate.provider=openweathermap",
                        "services.weather.url=https://api.openweathermap.org",
                        "services.weather.key=test-key")
                .run(context -> {
                    assertThat(context).hasSingleBean(ClimateDataProviderPort.class);
                    assertThat(context.getBean(ClimateDataProviderPort.class)).isInstanceOf(OpenWeatherMapAdapter.class);
                    assertThat(context).doesNotHaveBean(DummyClimateDataAdapter.class);
                });
    }

    @Test
    @DisplayName("weather-enabled profile with explicit dummy provider keeps dummy adapter")
    void weatherEnabledProfileWithDummyProviderKeepsDummyAdapter() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=weather-enabled",
                        "simulation.climate.provider=dummy")
                .run(context -> {
                    assertThat(context).hasSingleBean(ClimateDataProviderPort.class);
                    assertThat(context.getBean(ClimateDataProviderPort.class)).isInstanceOf(DummyClimateDataAdapter.class);
                    assertThat(context).doesNotHaveBean(OpenWeatherMapAdapter.class);
                });
    }
}
