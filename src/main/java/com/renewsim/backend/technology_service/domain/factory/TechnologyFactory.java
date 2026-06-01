package com.renewsim.backend.technology_service.domain.factory;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.domain.policy.TechnologyPolicy;

import java.math.BigDecimal;

/**
 * Factory for creating valid Technology domain objects.
 * Ensures all Value Objects are validated upon creation and
 * business-level consistency is checked through TechnologyPolicy.
 */
public final class TechnologyFactory {

    private TechnologyFactory() {
    }

    /**
     * Creates a fully validated Technology aggregate from raw primitive input.
     */
    public static Technology create(
            String name,
            double efficiency,
            double installationCost,
            double maintenanceCost,
            double environmentalImpact,
            double co2Reduction,
            double capacityFactor,
            String energyType) {
        try {
            Technology technology = new Technology(
                    name,
                    EnergyType.fromString(energyType),
                    new Efficiency(efficiency),
                    new InstallationCost(BigDecimal.valueOf(installationCost)),
                    new MaintenanceCost(BigDecimal.valueOf(maintenanceCost)),
                    new EnvironmentalImpact(environmentalImpact),
                    new Co2Reduction(co2Reduction),
                    new CapacityFactor(capacityFactor));

            // Business-level validation (cross-field consistency)
            TechnologyPolicy.validateCompatibility(technology);

            return technology;

        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidTechnologyParameterException("Invalid technology parameters: " + ex.getMessage());
        }
    }
}
