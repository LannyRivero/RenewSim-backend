package com.renewsim.backend.simulation_service.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ClimateConfigurationValidatorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(TestConfig.class);

    @Test
    @DisplayName("local profile allows openweathermap fallback configuration")
    void localProfileAllowsFallbackConfiguration() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=local",
                        "simulation.climate.provider=openweathermap",
                        "services.weather.key=dummy-key")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    @DisplayName("stage profile rejects dummy provider")
    void stageProfileRejectsDummyProvider() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=stage",
                        "simulation.climate.provider=dummy")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("Stage/prod require a real simulation climate provider; dummy is not allowed.");
                });
    }

    @Test
    @DisplayName("prod profile rejects openweathermap without real api key")
    void prodProfileRejectsMissingOpenWeatherKey() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=prod",
                        "simulation.climate.provider=openweathermap",
                        "services.weather.key=dummy-key")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("Stage/prod require OPENWEATHER_API_KEY when simulation.climate.provider=openweathermap.");
                });
    }

    @Test
    @DisplayName("stage profile accepts openweathermap with real api key")
    void stageProfileAcceptsRealOpenWeatherKey() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=stage",
                        "simulation.climate.provider=openweathermap",
                        "services.weather.url=https://api.openweathermap.org",
                        "services.weather.key=real-key-123")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    @DisplayName("stage profile rejects non-https weather url")
    void stageProfileRejectsNonHttpsWeatherUrl() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=stage",
                        "simulation.climate.provider=openweathermap",
                        "services.weather.url=http://api.openweathermap.org",
                        "services.weather.key=real-key-123")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("Stage/prod require services.weather.url to use HTTPS for OpenWeatherMap.");
                });
    }

    @Test
    @DisplayName("stage profile rejects unapproved weather host")
    void stageProfileRejectsUnapprovedWeatherHost() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=stage",
                        "simulation.climate.provider=openweathermap",
                        "services.weather.url=https://169.254.169.254",
                        "services.weather.key=real-key-123")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("Stage/prod require services.weather.url to point to an approved OpenWeatherMap host.");
                });
    }

    @Configuration
    @ConfigurationPropertiesScan(basePackageClasses = {
            SimulationClimateProperties.class,
            WeatherServiceProperties.class
    })
    static class TestConfig {
        @Bean
        ClimateConfigurationValidator climateConfigurationValidator(
                SimulationClimateProperties simulationClimateProperties,
                WeatherServiceProperties weatherServiceProperties,
                org.springframework.core.env.Environment environment) {
            return new ClimateConfigurationValidator(
                    simulationClimateProperties,
                    weatherServiceProperties,
                    environment);
        }
    }
}
