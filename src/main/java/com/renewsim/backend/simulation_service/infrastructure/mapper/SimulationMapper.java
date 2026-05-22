package com.renewsim.backend.simulation_service.infrastructure.mapper;

import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.*;
import com.renewsim.backend.simulation_service.infrastructure.persistence.entity.SimulationEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * SimulationMapper
 *
 * Responsibility: translate between persistence entities and immutable domain
 * models.
 * Designed for Clean Architecture & DDD compatibility.
 *
 * Notes:
 * - ENTITY → DOMAIN is implemented manually to preserve immutability.
 * - DOMAIN → ENTITY uses MapStruct mapping expressions.
 * - Compatible with MapStruct 1.6+ / Java 17+.
 */
@Mapper(componentModel = "spring", implementationName = "SimulationMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SimulationMapper {

    // ============================================================
    // ENTITY → DOMAIN (manual)
    // ============================================================
    default Simulation toDomain(SimulationEntity entity) {
        if (entity == null) {
            return null;
        }

        String resolvedLocation = firstNonBlank(entity.getLocation(), entity.getLocationName());
        double resolvedProjectSize = firstPositive(
                entity.getProjectSize(),
                entity.getCapacityKw(),
                1.0);
        double resolvedBudget = firstPositive(
                entity.getBudget(),
                entity.getInitialInvestment(),
                entity.getTotalCost(),
                1.0);
        double resolvedEstimatedEnergy = firstNonNull(entity.getEstimatedEnergy(), entity.getEnergyGenerated(), 0.0);
        String resolvedCreatedBy = firstNonBlank(entity.getCreatedBy(), "system");
        java.util.List<Long> resolvedTechnologyIds = entity.getTechnologyIds() != null
                ? entity.getTechnologyIds()
                : new java.util.ArrayList<>();

        return new Simulation(
                entity.getId(),
                resolvedLocation,
                parseEnergyType(entity.getEnergyType()),
                new ProjectSize(resolvedProjectSize),
                new Budget(resolvedBudget),
                new EnergyOutput(resolvedEstimatedEnergy),
                new CO2Reduction(entity.getCo2Reduction()),
                new ClimateData(0, 0, 0),
                resolvedTechnologyIds,
                resolvedCreatedBy,
                entity.getCreatedAt());
    }

    // ============================================================
    // DOMAIN → ENTITY (MapStruct)
    // ============================================================
    @Mapping(target = "location", expression = "java(domain.location())")
    @Mapping(target = "energyType", expression = "java(domain.energyType().name())")
    @Mapping(target = "projectSize", expression = "java(domain.projectSize().value())")
    @Mapping(target = "budget", expression = "java(domain.budget().value())")

    @Mapping(target = "estimatedEnergy", expression = "java(domain.energyOutput().kwhPerYear())")
    @Mapping(target = "co2Reduction", expression = "java(domain.co2Reduction().tonsPerYear())")
    @Mapping(target = "technologyIds", expression = "java(domain.technologyIds() != null ? new java.util.ArrayList<>(domain.technologyIds()) : new java.util.ArrayList<>())")
    @Mapping(target = "createdAt", expression = "java(domain.createdAt())")
    @Mapping(target = "createdBy", expression = "java(domain.createdBy())")
    SimulationEntity toEntity(Simulation domain);

    // ============================================================
    // Helpers
    // ============================================================
    default EnergyType parseEnergyType(String energyTypeRaw) {
        if (energyTypeRaw == null || energyTypeRaw.isBlank()) {
            return EnergyType.SOLAR;
        }
        try {
            return EnergyType.valueOf(energyTypeRaw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unknown energy type: " + energyTypeRaw, ex);
        }
    }

    default String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "unknown";
    }

    default double firstPositive(Double primary, Double fallback, double defaultValue) {
        if (primary != null && primary > 0) {
            return primary;
        }
        if (fallback != null && fallback > 0) {
            return fallback;
        }
        return defaultValue;
    }

    default double firstPositive(Double primary, Double fallbackOne, Double fallbackTwo, double defaultValue) {
        if (primary != null && primary > 0) {
            return primary;
        }
        if (fallbackOne != null && fallbackOne > 0) {
            return fallbackOne;
        }
        if (fallbackTwo != null && fallbackTwo > 0) {
            return fallbackTwo;
        }
        return defaultValue;
    }

    default double firstNonNull(Double primary, Double fallback, double defaultValue) {
        if (primary != null) {
            return primary;
        }
        if (fallback != null) {
            return fallback;
        }
        return defaultValue;
    }

    List<Simulation> toDomainList(List<SimulationEntity> entities);
}
