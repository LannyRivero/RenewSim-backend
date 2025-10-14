package com.renewsim.backend.technology_service.domain.model;

import java.util.Objects;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.domain.policy.TechnologyPolicy;

/**
 * Domain entity representing a renewable technology.
 * Uses Value Objects to ensure domain invariants.
 * Fully immutable and validated by TechnologyPolicy for business-level rules.
 */
public class Technology {

    private final String name;
    private final EnergyType energyType;
    private final Efficiency efficiency;
    private final InstallationCost installationCost;
    private final MaintenanceCost maintenanceCost;
    private final EnvironmentalImpact environmentalImpact;
    private final Co2Reduction co2Reduction;
    private final EnergyProduction energyProduction;

    public Technology(
            String name,
            EnergyType energyType,
            Efficiency efficiency,
            InstallationCost installationCost,
            MaintenanceCost maintenanceCost,
            EnvironmentalImpact environmentalImpact,
            Co2Reduction co2Reduction,
            EnergyProduction energyProduction) {

        if (name == null || name.isBlank()) {
            throw new InvalidTechnologyParameterException("Technology name cannot be null or blank");
        }

        this.name = name.trim();
        this.energyType = Objects.requireNonNull(energyType);
        this.efficiency = Objects.requireNonNull(efficiency);
        this.installationCost = Objects.requireNonNull(installationCost);
        this.maintenanceCost = Objects.requireNonNull(maintenanceCost);
        this.environmentalImpact = Objects.requireNonNull(environmentalImpact);
        this.co2Reduction = Objects.requireNonNull(co2Reduction);
        this.energyProduction = Objects.requireNonNull(energyProduction);

        // Business-level validation (e.g., combined constraints)
        TechnologyPolicy.validateCompatibility(this);
    }

    public String getName() {
        return name;
    }

    public EnergyType getEnergyType() {
        return energyType;
    }

    public Efficiency getEfficiency() {
        return efficiency;
    }

    public InstallationCost getInstallationCost() {
        return installationCost;
    }

    public MaintenanceCost getMaintenanceCost() {
        return maintenanceCost;
    }

    public EnvironmentalImpact getEnvironmentalImpact() {
        return environmentalImpact;
    }

    public Co2Reduction getCo2Reduction() {
        return co2Reduction;
    }

    public EnergyProduction getEnergyProduction() {
        return energyProduction;
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
        return "Technology{name='%s', efficiency=%s, cost=%s}"
                .formatted(name, efficiency.value(), installationCost.value());
    }
}
