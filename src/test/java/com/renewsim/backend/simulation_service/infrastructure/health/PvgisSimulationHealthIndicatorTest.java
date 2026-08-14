package com.renewsim.backend.simulation_service.infrastructure.health;

import com.renewsim.backend.simulation_service.infrastructure.config.PvgisProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

class PvgisSimulationHealthIndicatorTest {

    @Test
    @DisplayName("health is up when pvgis url is valid")
    void healthIsUpWhenPvgisUrlIsValid() {
        PvgisSimulationHealthIndicator indicator = new PvgisSimulationHealthIndicator(
                new PvgisProperties("https://re.jrc.ec.europa.eu", null, null));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("provider", "pvgis");
    }

    @Test
    @DisplayName("health is down when pvgis url is invalid")
    void healthIsDownWhenPvgisUrlIsInvalid() {
        PvgisSimulationHealthIndicator indicator = new PvgisSimulationHealthIndicator(
                new PvgisProperties("ftp://re.jrc.ec.europa.eu", null, null));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "invalid_pvgis_url");
    }
}
