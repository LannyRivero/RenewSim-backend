package com.renewsim.backend.simulation_service.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.renewsim.backend.simulation_service.domain.model.vo.*;

/**
 * Root Aggregate for Simulation domain.
 *
 * Immutable aggregate with controlled internal mutation
 * only for technologyIds to support persistence synchronization.
 */
public final class Simulation {

    private final Long id;
    private final String name;
    private final String location;
    private final double latitude;
    private final double longitude;
    private final EnergyType energyType;
    private final ProjectSize projectSize;
    private final Budget budget;
    private final EnergyOutput energyOutput;
    private final CO2Reduction co2Reduction;
    private final ClimateData climateData;
    private final LocalDateTime createdAt;
    private final String createdBy;

    private final List<Long> technologyIds;

    public Simulation(
            Long id,
            String name,
            String location,
            double latitude,
            double longitude,
            EnergyType energyType,
            ProjectSize projectSize,
            Budget budget,
            EnergyOutput energyOutput,
            CO2Reduction co2Reduction,
            ClimateData climateData,
            List<Long> technologyIds,
            String createdBy,
            LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.energyType = energyType;
        this.projectSize = projectSize;
        this.budget = budget;
        this.energyOutput = energyOutput;
        this.co2Reduction = co2Reduction;
        this.climateData = climateData;
        this.technologyIds = (technologyIds != null)
                ? new ArrayList<>(technologyIds)
                : new ArrayList<>();
        this.createdBy = createdBy;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    // ==========================
    // Getters
    // ==========================
    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String location() {
        return location;
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    public EnergyType energyType() {
        return energyType;
    }

    public ProjectSize projectSize() {
        return projectSize;
    }

    public Budget budget() {
        return budget;
    }

    public EnergyOutput energyOutput() {
        return energyOutput;
    }

    public CO2Reduction co2Reduction() {
        return co2Reduction;
    }

    public ClimateData climateData() {
        return climateData;
    }

    public String createdBy() {
        return createdBy;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    /**
     * Returns an unmodifiable view to prevent external modification.
     */
    public List<Long> technologyIds() {
        return Collections.unmodifiableList(technologyIds);
    }

    // ==========================
    // Domain Behavior
    // ==========================

    /**
     * Assigns technologies to this simulation.
     * Internal mutation is allowed only for persistence synchronization.
     */
    public Simulation assignTechnologies(List<Long> newTechnologyIds) {
        if (newTechnologyIds == null || newTechnologyIds.isEmpty()) {
            throw new IllegalArgumentException("Technology list cannot be null or empty");
        }

        this.technologyIds.clear();
        this.technologyIds.addAll(newTechnologyIds);
        return this;
    }

    /**
     * Returns a new Simulation instance enriched with calculated results.
     * Preserves immutability of derived values.
     */
    public Simulation withCalculatedResults(
            EnergyOutput newEnergyOutput,
            CO2Reduction newCo2Reduction) {
        return new Simulation(
                this.id,
                this.name,
                this.location,
                this.latitude,
                this.longitude,
                this.energyType,
                this.projectSize,
                this.budget,
                newEnergyOutput,
                newCo2Reduction,
                this.climateData,
                this.technologyIds,
                this.createdBy,
                this.createdAt);
    }
}
