package com.renewsim.backend.simulation_service.infrastructure.health;

import com.renewsim.backend.simulation_service.infrastructure.config.PvgisProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for the PVGIS solar resource integration (actuator name {@code pvgisSimulation}).
 *
 * <p>PVGIS has no API key, so the only meaningful configuration check is that the configured URL is
 * a valid HTTP/HTTPS endpoint; anything else keeps the indicator UP, leaving availability checks to
 * the calling service.</p>
 */
@Component("pvgisSimulation")
public class PvgisSimulationHealthIndicator implements HealthIndicator {

    private final PvgisProperties pvgisProperties;

    public PvgisSimulationHealthIndicator(PvgisProperties pvgisProperties) {
        this.pvgisProperties = pvgisProperties;
    }

    @Override
    public Health health() {
        if (!SimulationHealthSupport.hasValidHttpUri(pvgisProperties.url())) {
            return Health.down()
                    .withDetail("provider", "pvgis")
                    .withDetail("reason", "invalid_pvgis_url")
                    .build();
        }
        return Health.up().withDetail("provider", "pvgis").build();
    }
}
