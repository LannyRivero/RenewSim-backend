package com.renewsim.backend.technology_service.domain.model;

import java.util.Objects;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.policy.TechnologyPolicy;

/**
 * Domain Value Object representing a renewable technology.
 * Fully immutable and validated by TechnologyPolicy.
 */

public record Technology(
        String name,
        double efficiency,
        double installationCost,
        double maintenanceCost,
        double environmentalImpact,
        double co2Reduction,
        double energyProduction,
        String energyType) {

    public Technology {
        try {
            TechnologyPolicy.validateName(name);
            TechnologyPolicy.validateEfficiency(efficiency);
            TechnologyPolicy.validateCost(installationCost, maintenanceCost);
            TechnologyPolicy.validateEnvironmentalImpact(environmentalImpact);
            TechnologyPolicy.validateCo2Reduction(co2Reduction);
            TechnologyPolicy.validateEnergyProduction(energyProduction);
        } catch (IllegalArgumentException ex) {
            throw new InvalidTechnologyParameterException("Invalid technology parameter: " + ex.getMessage());

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Technology t))
            return false;
        return name.equalsIgnoreCase(t.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return "Technology{name='%s', efficiency=%.2f, cost=%.2f}".formatted(name, efficiency, installationCost);
    }
}