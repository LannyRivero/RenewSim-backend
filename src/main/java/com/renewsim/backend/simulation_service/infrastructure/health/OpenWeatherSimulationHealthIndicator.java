package com.renewsim.backend.simulation_service.infrastructure.health;

import com.renewsim.backend.simulation_service.infrastructure.config.SimulationClimateProperties;
import com.renewsim.backend.simulation_service.infrastructure.config.WeatherServiceProperties;
import com.renewsim.backend.simulation_service.location_lookup.application.port.out.LocationLookupProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component("openWeatherSimulation")
public class OpenWeatherSimulationHealthIndicator implements HealthIndicator {

    private final SimulationClimateProperties climateProperties;
    private final WeatherServiceProperties weatherProperties;
    private final ObjectProvider<LocationLookupProvider> locationLookupProvider;
    private final Environment environment;

    public OpenWeatherSimulationHealthIndicator(
            SimulationClimateProperties climateProperties,
            WeatherServiceProperties weatherProperties,
            ObjectProvider<LocationLookupProvider> locationLookupProvider,
            Environment environment) {
        this.climateProperties = climateProperties;
        this.weatherProperties = weatherProperties;
        this.locationLookupProvider = locationLookupProvider;
        this.environment = environment;
    }

    @Override
    public Health health() {
        String provider = SimulationHealthSupport.normalize(climateProperties.provider());
        boolean strictProfile = environment.acceptsProfiles(Profiles.of("stage", "prod"));

        if (!provider.equals("openweathermap")) {
            return Health.unknown()
                    .withDetail("provider", "openweathermap")
                    .withDetail("reason", "provider_not_active")
                    .build();
        }

        Health.Builder builder = Health.up()
                .withDetail("provider", "openweathermap")
                .withDetail("strictProfile", strictProfile)
                .withDetail("locationLookupProviderPresent", locationLookupProvider.getIfAvailable() != null);

        if (locationLookupProvider.getIfAvailable() == null) {
            return builder.down().withDetail("reason", "missing_location_lookup_provider").build();
        }
        if (!SimulationHealthSupport.hasValidHttpUri(weatherProperties.url())) {
            return builder.down().withDetail("reason", "invalid_openweather_url").build();
        }
        if (strictProfile && SimulationHealthSupport.isMissingOrDummy(weatherProperties.key())) {
            return builder.down().withDetail("reason", "missing_openweather_api_key").build();
        }

        return builder.build();
    }
}
