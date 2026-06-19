package com.renewsim.backend.simulation_service.infrastructure.config;

import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.dummy.DummyClimateDataAdapter;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.external.OpenWeatherMapAdapter;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class ClimateProviderWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    RestTemplateAutoConfiguration.class,
                    DummyClimateDataAdapter.class,
                    OpenWeatherClientConfig.class,
                    OpenWeatherMapAdapter.class));

    @Test
    @DisplayName("dummy provider keeps dummy climate adapter regardless of profile")
    void dummyProviderKeepsDummyClimateProvider() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=local",
                        "simulation.climate.provider=dummy")
                .run(context -> {
                    assertThat(context).hasSingleBean(ClimateDataProviderPort.class);
                    assertThat(context.getBean(ClimateDataProviderPort.class)).isInstanceOf(DummyClimateDataAdapter.class);
                    assertThat(context).doesNotHaveBean(OpenWeatherMapAdapter.class);
                });
    }

    @Test
    @DisplayName("openweathermap provider uses OpenWeatherMap without requiring extra profile")
    void openWeatherProviderUsesOpenWeatherMapWhenConfigured() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=local",
                        "simulation.climate.provider=openweathermap",
                        "services.weather.url=https://api.openweathermap.org",
                        "services.weather.key=test-key")
                .run(context -> {
                    assertThat(context).hasSingleBean(ClimateDataProviderPort.class);
                    assertThat(context.getBean(ClimateDataProviderPort.class)).isInstanceOf(OpenWeatherMapAdapter.class);
                    assertThat(context).hasBean("openWeatherRestTemplate");
                    assertThat(context.getBean("openWeatherRestTemplate", RestTemplate.class)).isNotNull();
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
