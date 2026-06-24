package com.renewsim.backend.simulation_service.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.renewsim.backend.shared.domain.vo.Location;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationParameterException;
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

    private Simulation(Builder builder) {
        validate(builder);

        this.id = builder.id;
        this.name = builder.name.trim();
        this.location = builder.location.trim();
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.energyType = builder.energyType;
        this.projectSize = builder.projectSize;
        this.budget = builder.budget;
        this.energyOutput = builder.energyOutput;
        this.co2Reduction = builder.co2Reduction;
        this.climateData = builder.climateData;
        this.technologyIds = new ArrayList<>(builder.technologyIds);
        this.createdBy = builder.createdBy.trim();
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Simulation create(
            String name,
            String location,
            double latitude,
            double longitude,
            EnergyType energyType,
            ProjectSize projectSize,
            Budget budget,
            ClimateData climateData,
            List<Long> technologyIds,
            String createdBy) {
        return builder()
                .name(name)
                .location(location)
                .latitude(latitude)
                .longitude(longitude)
                .energyType(energyType)
                .projectSize(projectSize)
                .budget(budget)
                .energyOutput(new EnergyOutput(0))
                .co2Reduction(new CO2Reduction(0))
                .climateData(climateData)
                .technologyIds(technologyIds)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Simulation reconstitute(
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
        return builder()
                .id(id)
                .name(name)
                .location(location)
                .latitude(latitude)
                .longitude(longitude)
                .energyType(energyType)
                .projectSize(projectSize)
                .budget(budget)
                .energyOutput(energyOutput)
                .co2Reduction(co2Reduction)
                .climateData(climateData)
                .technologyIds(technologyIds)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .build();
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
     * Returns a new aggregate instance with the assigned technologies.
     */
    public Simulation assignTechnologies(List<Long> newTechnologyIds) {
        if (newTechnologyIds == null || newTechnologyIds.isEmpty()) {
            throw new IllegalArgumentException("Technology list cannot be null or empty");
        }

        return toBuilder()
                .technologyIds(newTechnologyIds)
                .build();
    }

    /**
     * Returns a new Simulation instance enriched with calculated results.
     * Preserves immutability of derived values.
     */
    public Simulation withCalculatedResults(
            EnergyOutput newEnergyOutput,
            CO2Reduction newCo2Reduction) {
        return toBuilder()
                .energyOutput(newEnergyOutput)
                .co2Reduction(newCo2Reduction)
                .build();
    }

    public Simulation revise(
            String name,
            String location,
            double latitude,
            double longitude,
            EnergyType energyType,
            ProjectSize projectSize,
            Budget budget,
            ClimateData climateData,
            List<Long> technologyIds) {
        return toBuilder()
                .name(name)
                .location(location)
                .latitude(latitude)
                .longitude(longitude)
                .energyType(energyType)
                .projectSize(projectSize)
                .budget(budget)
                .climateData(climateData)
                .technologyIds(technologyIds)
                .build();
    }

    private Builder toBuilder() {
        return builder()
                .id(id)
                .name(name)
                .location(location)
                .latitude(latitude)
                .longitude(longitude)
                .energyType(energyType)
                .projectSize(projectSize)
                .budget(budget)
                .energyOutput(energyOutput)
                .co2Reduction(co2Reduction)
                .climateData(climateData)
                .technologyIds(technologyIds)
                .createdBy(createdBy)
                .createdAt(createdAt);
    }

    private static void validate(Builder builder) {
        if (builder.name == null || builder.name.isBlank()) {
            throw new InvalidSimulationParameterException("Simulation name cannot be blank");
        }
        if (builder.location == null || builder.location.isBlank()) {
            throw new InvalidSimulationParameterException("Simulation location cannot be blank");
        }
        if (builder.createdBy == null || builder.createdBy.isBlank()) {
            throw new InvalidSimulationParameterException("Simulation owner cannot be blank");
        }

        new Location(builder.latitude, builder.longitude);
        Objects.requireNonNull(builder.energyType, "energyType must not be null");
        Objects.requireNonNull(builder.projectSize, "projectSize must not be null");
        Objects.requireNonNull(builder.budget, "budget must not be null");
        Objects.requireNonNull(builder.energyOutput, "energyOutput must not be null");
        Objects.requireNonNull(builder.co2Reduction, "co2Reduction must not be null");
    }

    public static final class Builder {
        private Long id;
        private String name;
        private String location;
        private double latitude;
        private double longitude;
        private EnergyType energyType;
        private ProjectSize projectSize;
        private Budget budget;
        private EnergyOutput energyOutput;
        private CO2Reduction co2Reduction;
        private ClimateData climateData;
        private List<Long> technologyIds = List.of();
        private String createdBy;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder latitude(double latitude) { this.latitude = latitude; return this; }
        public Builder longitude(double longitude) { this.longitude = longitude; return this; }
        public Builder energyType(EnergyType energyType) { this.energyType = energyType; return this; }
        public Builder projectSize(ProjectSize projectSize) { this.projectSize = projectSize; return this; }
        public Builder budget(Budget budget) { this.budget = budget; return this; }
        public Builder energyOutput(EnergyOutput energyOutput) { this.energyOutput = energyOutput; return this; }
        public Builder co2Reduction(CO2Reduction co2Reduction) { this.co2Reduction = co2Reduction; return this; }
        public Builder climateData(ClimateData climateData) { this.climateData = climateData; return this; }
        public Builder technologyIds(List<Long> technologyIds) {
            this.technologyIds = technologyIds == null ? List.of() : List.copyOf(technologyIds);
            return this;
        }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Simulation build() {
            return new Simulation(this);
        }
    }
}
