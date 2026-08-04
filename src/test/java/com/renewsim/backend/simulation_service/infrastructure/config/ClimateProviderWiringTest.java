package com.renewsim.backend.simulation_service.infrastructure.config;

import com.renewsim.backend.simulation_service.infrastructure.adapter.out.external.OpenWeatherMapAdapter;
import com.renewsim.backend.simulation_service.location_lookup.application.LocationLookupService;
import com.renewsim.backend.simulation_service.location_lookup.application.port.out.LocationLookupProvider;
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
                    OpenWeatherClientConfig.class,
                    LocationLookupService.class,
                    OpenWeatherMapAdapter.class));

    @Test
    @DisplayName("missing provider does not instantiate a location lookup provider")
    void missingProviderDoesNotInstantiateLocationLookupProvider() {
        contextRunner
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LocationLookupProvider.class);
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
                    assertThat(context).hasSingleBean(LocationLookupProvider.class);
                    assertThat(context.getBean(LocationLookupProvider.class)).isInstanceOf(OpenWeatherMapAdapter.class);
                    assertThat(context).hasSingleBean(LocationLookupService.class);
                    assertThat(context).hasBean("openWeatherRestTemplate");
                    assertThat(context.getBean("openWeatherRestTemplate", RestTemplate.class)).isNotNull();
                });
    }
}
