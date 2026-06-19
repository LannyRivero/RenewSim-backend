package com.renewsim.backend.simulation_service.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds climate-provider configuration used by the simulation module.
 *
 * <p>The selected provider controls which climate adapter is activated at
 * runtime and which startup validations must pass.</p>
 */
@ConfigurationProperties(prefix = "simulation.climate")
public record SimulationClimateProperties(String provider) {
}
