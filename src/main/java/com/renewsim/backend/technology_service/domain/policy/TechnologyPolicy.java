package com.renewsim.backend.technology_service.domain.policy;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;

public final class TechnologyPolicy {

    private TechnologyPolicy() {}

    public static void validateName(String name) {
        if (name == null || name.isBlank())
            throw new InvalidTechnologyParameterException("Technology name cannot be null or blank");
    }

    public static void validateEfficiency(double efficiency) {
        if (efficiency < 0 || efficiency > 1)
            throw new InvalidTechnologyParameterException("Efficiency must be between 0 and 1");
    }

    public static void validateCost(double installationCost, double maintenanceCost) {
        if (installationCost <= 0 || maintenanceCost <= 0)
            throw new InvalidTechnologyParameterException("Costs must be positive");
    }

    public static void validateEnvironmentalImpact(double impact) {
        if (impact < 0 || impact > 100)
            throw new InvalidTechnologyParameterException("Environmental impact must be between 0 and 100");
    }

    public static void validateCo2Reduction(double reduction) {
        if (reduction < 0)
            throw new InvalidTechnologyParameterException("CO₂ reduction cannot be negative");
    }

    public static void validateEnergyProduction(double production) {
        if (production <= 0)
            throw new InvalidTechnologyParameterException("Energy production must be positive");
    }
}

