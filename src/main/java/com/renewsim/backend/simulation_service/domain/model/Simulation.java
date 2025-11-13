package com.renewsim.backend.simulation_service.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.renewsim.backend.simulation_service.domain.model.vo.*;

/**
 * 🧠 Root Aggregate for Simulation domain.
 * Immutable-like aggregate, but allows controlled mutation of technologyIds
 * to enable persistence synchronization.
 */
public final class Simulation {

    private final Long id;
    private final String location;
    private final EnergyType energyType;
    private final ProjectSize projectSize;
    private final Budget budget;
    private final EnergyOutput energyOutput;
    private final CO2Reduction co2Reduction;
    private final ClimateData climateData;
    private final LocalDateTime createdAt;
    private final String createdBy;

    // 👇 Se mantiene mutable internamente para compatibilidad con JPA
    private List<Long> technologyIds;

    public Simulation(
        Long id,
        String location,
        EnergyType energyType,
        ProjectSize projectSize,
        Budget budget,
        EnergyOutput energyOutput,
        CO2Reduction co2Reduction,
        ClimateData climateData,
        List<Long> technologyIds,
        String createdBy,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.location = location;
        this.energyType = energyType;
        this.projectSize = projectSize;
        this.budget = budget;
        this.energyOutput = energyOutput;
        this.co2Reduction = co2Reduction;
        this.climateData = climateData;
        this.technologyIds = (technologyIds != null) ? new ArrayList<>(technologyIds) : new ArrayList<>();
        this.createdBy = createdBy;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    // ==========================
    // ✅ Getters (read-only)
    // ==========================
    public Long id() { return id; }
    public String location() { return location; }
    public EnergyType energyType() { return energyType; }
    public ProjectSize projectSize() { return projectSize; }
    public Budget budget() { return budget; }
    public EnergyOutput energyOutput() { return energyOutput; }
    public CO2Reduction co2Reduction() { return co2Reduction; }
    public ClimateData climateData() { return climateData; }
    public String createdBy() { return createdBy; }
    public LocalDateTime createdAt() { return createdAt; }

    /**
     * Returns an unmodifiable view to prevent external modification.
     */
    public List<Long> technologyIds() {
        return Collections.unmodifiableList(technologyIds);
    }

    // ==========================
    // 🧩 Domain Behavior
    // ==========================
    /**
     * Assigns technologies to this simulation.
     * Mutates the internal list for persistence consistency.
     */
    public Simulation assignTechnologies(List<Long> newTechnologyIds) {
        if (newTechnologyIds == null || newTechnologyIds.isEmpty()) {
            throw new IllegalArgumentException("Technology list cannot be null or empty");
        }

        this.technologyIds.clear();
        this.technologyIds.addAll(newTechnologyIds);
        return this;
    }
}
