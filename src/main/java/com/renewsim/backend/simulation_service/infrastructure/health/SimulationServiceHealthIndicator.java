package com.renewsim.backend.simulation_service.infrastructure.health;

import com.renewsim.backend.simulation_service.infrastructure.config.PvgisProperties;
import com.renewsim.backend.simulation_service.infrastructure.config.SimulationClimateProperties;
import com.renewsim.backend.simulation_service.infrastructure.config.WeatherServiceProperties;
import com.renewsim.backend.simulation_service.location_lookup.application.port.out.LocationLookupProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

@Component("simulationService")
public class SimulationServiceHealthIndicator implements HealthIndicator {

    private static final Set<String> STRICT_PROFILES = Set.of("stage", "prod");

    private final SimulationClimateProperties climateProperties;
    private final WeatherServiceProperties weatherProperties;
    private final PvgisProperties pvgisProperties;
    private final ObjectProvider<LocationLookupProvider> locationLookupProvider;
    private final Environment environment;

    public SimulationServiceHealthIndicator(
            SimulationClimateProperties climateProperties,
            WeatherServiceProperties weatherProperties,
            PvgisProperties pvgisProperties,
            ObjectProvider<LocationLookupProvider> locationLookupProvider,
            Environment environment) {
        this.climateProperties = climateProperties;
        this.weatherProperties = weatherProperties;
        this.pvgisProperties = pvgisProperties;
        this.locationLookupProvider = locationLookupProvider;
        this.environment = environment;
    }

    @Override
    public Health health() {
        String provider = normalize(climateProperties.provider());
        boolean strictProfile = STRICT_PROFILES.stream()
                .anyMatch(profile -> environment.acceptsProfiles(Profiles.of(profile)));

        Health.Builder builder = Health.up()
                .withDetail("climateProvider", provider.isBlank() ? "unset" : provider)
                .withDetail("strictProfile", strictProfile)
                .withDetail("locationLookupProviderPresent", locationLookupProvider.getIfAvailable() != null)
                .withDetail("pvgisConfigured", hasValidUri(pvgisProperties.url()));

        if (!provider.isBlank() && !provider.equals("dummy") && !provider.equals("openweathermap")) {
            return builder.down()
                    .withDetail("reason", "unsupported_climate_provider")
                    .build();
        }

        if (provider.equals("openweathermap")) {
            if (locationLookupProvider.getIfAvailable() == null) {
                return builder.down()
                        .withDetail("reason", "missing_location_lookup_provider")
                        .build();
            }
            if (!hasValidUri(weatherProperties.url())) {
                return builder.down()
                        .withDetail("reason", "invalid_openweather_url")
                        .build();
            }
            if (strictProfile && isMissingOrDummy(weatherProperties.key())) {
                return builder.down()
                        .withDetail("reason", "missing_openweather_api_key")
                        .build();
            }
        }

        if (!hasValidUri(pvgisProperties.url())) {
            return builder.down()
                    .withDetail("reason", "invalid_pvgis_url")
                    .build();
        }

        return builder.build();
    }

    private boolean hasValidUri(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = normalize(uri.getScheme());
            return (scheme.equals("http") || scheme.equals("https")) && uri.getHost() != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isMissingOrDummy(String key) {
        String normalized = normalize(key);
        return normalized.isBlank() || normalized.equals("dummy-key");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
