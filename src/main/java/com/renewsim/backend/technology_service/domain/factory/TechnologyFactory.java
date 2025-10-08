package com.renewsim.backend.technology_service.domain.factory;

import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.policy.TechnologyPolicy;
import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;

public final class TechnologyFactory {

    private TechnologyFactory() {}

    public static Technology create(
            String name,
            double efficiency,
            double installationCost,
            double maintenanceCost,
            double environmentalImpact,
            double co2Reduction,
            double energyProduction,
            String energyType) {

        try {
            TechnologyPolicy.validateName(name);
            TechnologyPolicy.validateEfficiency(efficiency);
            TechnologyPolicy.validateCost(installationCost, maintenanceCost);
            TechnologyPolicy.validateEnvironmentalImpact(environmentalImpact);
            TechnologyPolicy.validateCo2Reduction(co2Reduction);
            TechnologyPolicy.validateEnergyProduction(energyProduction);

            return new Technology(
                    name,
                    efficiency,
                    installationCost,
                    maintenanceCost,
                    environmentalImpact,
                    co2Reduction,
                    energyProduction,
                    energyType
            );

        } catch (IllegalArgumentException ex) {
            throw new InvalidTechnologyParameterException(ex.getMessage());
        }
    }
}
