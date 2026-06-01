package com.renewsim.backend.technology_service.domain.model;

import java.util.Objects;
import java.time.Instant;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.domain.policy.TechnologyPolicy;

/**
 * Domain entity representing a renewable technology.
 * Uses Value Objects to ensure domain invariants.
 * Fully immutable and validated by TechnologyPolicy for business-level rules.
 */
public class Technology {

    private final Long id;
    private final String name;
    private final EnergyType energyType;
    private final Efficiency efficiency;
    private final InstallationCost baseCostPerKw;
    private final Integer lifespanYears;
    private final MaintenanceCost maintenanceCostPct;
    private final String description;
    private final boolean isActive;
    private final Instant createdAt;
    private final Instant updatedAt;

    private final EnvironmentalImpact environmentalImpact;
    private final Co2Reduction co2Reduction;
    private final CapacityFactor capacityFactor;

    public Technology(
            Long id,
            String name,
            EnergyType energyType,
            Efficiency efficiency,
            InstallationCost baseCostPerKw,
            Integer lifespanYears,
            MaintenanceCost maintenanceCostPct,
            String description,
            boolean isActive,
            Instant createdAt,
            Instant updatedAt,
            EnvironmentalImpact environmentalImpact,
            Co2Reduction co2Reduction,
            CapacityFactor capacityFactor) {

        if (name == null || name.isBlank()) {
            throw new InvalidTechnologyParameterException("Technology name cannot be null or blank");
        }
        if (lifespanYears == null || lifespanYears <= 0) {
            throw new InvalidTechnologyParameterException("Lifespan years must be greater than zero");
        }

        this.id = id;
        this.name = name.trim();
        this.energyType = Objects.requireNonNull(energyType);
        this.efficiency = Objects.requireNonNull(efficiency);
        this.baseCostPerKw = Objects.requireNonNull(baseCostPerKw);
        this.lifespanYears = lifespanYears;
        this.maintenanceCostPct = Objects.requireNonNull(maintenanceCostPct);
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.environmentalImpact = Objects.requireNonNull(environmentalImpact);
        this.co2Reduction = Objects.requireNonNull(co2Reduction);
        this.capacityFactor = Objects.requireNonNull(capacityFactor);

        TechnologyPolicy.validateCompatibility(this);
    }

    public Technology(
            Long id,
            String name,
            EnergyType energyType,
            Efficiency efficiency,
            InstallationCost baseCostPerKw,
            MaintenanceCost maintenanceCost,
            EnvironmentalImpact environmentalImpact,
            Co2Reduction co2Reduction,
            CapacityFactor capacityFactor) {
        this(
                id,
                name,
                energyType,
                efficiency,
                baseCostPerKw,
                25,
                maintenanceCost,
                null,
                true,
                null,
                null,
                environmentalImpact,
                co2Reduction,
                capacityFactor);
    }

    public Technology(
            String name,
            EnergyType energyType,
            Efficiency efficiency,
            InstallationCost baseCostPerKw,
            MaintenanceCost maintenanceCost,
            EnvironmentalImpact environmentalImpact,
            Co2Reduction co2Reduction,
            CapacityFactor capacityFactor) {
        this(null, name, energyType, efficiency, baseCostPerKw, maintenanceCost, environmentalImpact, co2Reduction,
                capacityFactor);
    }

    public Long getId() {
        return id;
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
        return baseCostPerKw;
    }

    public MaintenanceCost getMaintenanceCost() {
        return maintenanceCostPct;
    }

    public InstallationCost getBaseCostPerKw() {
        return baseCostPerKw;
    }

    public Integer getLifespanYears() {
        return lifespanYears;
    }

    public MaintenanceCost getMaintenanceCostPct() {
        return maintenanceCostPct;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public EnvironmentalImpact getEnvironmentalImpact() {
        return environmentalImpact;
    }

    public Co2Reduction getCo2Reduction() {
        return co2Reduction;
    }

    public CapacityFactor getCapacityFactor() {
        return capacityFactor;
    }

    public Technology deactivate() {
        return new Technology(
                this.id,
                this.name,
                this.energyType,
                this.efficiency,
                this.baseCostPerKw,
                this.lifespanYears,
                this.maintenanceCostPct,
                this.description,
                false,
                this.createdAt,
                Instant.now(),
                this.environmentalImpact,
                this.co2Reduction,
                this.capacityFactor);
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
                .formatted(name, efficiency.value(), baseCostPerKw.value());
    }
}
