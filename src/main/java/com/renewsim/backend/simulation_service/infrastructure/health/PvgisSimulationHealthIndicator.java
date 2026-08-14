package com.renewsim.backend.simulation_service.infrastructure.health;

import com.renewsim.backend.simulation_service.infrastructure.config.PvgisProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

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
