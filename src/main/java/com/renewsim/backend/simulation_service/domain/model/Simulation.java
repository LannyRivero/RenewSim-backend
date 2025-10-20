package com.renewsim.backend.simulation_service.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import com.renewsim.backend.simulation_service.domain.model.vo.*;

/**
 * Root Aggregate for Simulation domain.
 * Pure domain entity: no framework annotations, no persistence logic.
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
    private final List<Long> technologyIds;

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
        this.technologyIds = technologyIds;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long id() { return id; }
    public String location() { return location; }
    public EnergyType energyType() { return energyType; }
    public ProjectSize projectSize() { return projectSize; }
    public Budget budget() { return budget; }
    public EnergyOutput energyOutput() { return energyOutput; }
    public CO2Reduction co2Reduction() { return co2Reduction; }
    public ClimateData climateData() { return climateData; }
    public List<Long> technologyIds() { return technologyIds; }
    public LocalDateTime createdAt() { return createdAt; }
}
