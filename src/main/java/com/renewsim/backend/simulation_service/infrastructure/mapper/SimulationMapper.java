package com.renewsim.backend.simulation_service.infrastructure.mapper;

import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.*;
import com.renewsim.backend.simulation_service.infrastructure.persistence.entity.SimulationEntity;
import org.mapstruct.*;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

        return Simulation.reconstitute(
                entity.getId(),
                firstNonBlank(entity.getName(), "Simulation " + entity.getId()),
                resolvedLocation,
                firstNonNull(entity.getLocationLat(), 0.0),
                firstNonNull(entity.getLocationLng(), 0.0),
                parseEnergyType(entity.getEnergyType()),
                new ProjectSize(resolvedProjectSize),
                new Budget(resolvedBudget),
                new EnergyOutput(resolvedEstimatedEnergy),
                new CO2Reduction(entity.getCo2Reduction()),
                parseClimateData(entity.getClimateData()),
                resolvedTechnologyIds,
                resolvedCreatedBy,
                entity.getCreatedAt());
    }

    // ============================================================
    // DOMAIN → ENTITY (MapStruct)
    // ============================================================
    @Mapping(target = "id", expression = "java(domain.id())")
    @Mapping(target = "location", expression = "java(domain.location())")
    @Mapping(target = "name", expression = "java(domain.name())")
    @Mapping(target = "energyType", expression = "java(domain.energyType().name())")
    @Mapping(target = "locationLat", expression = "java(domain.latitude())")
    @Mapping(target = "locationLng", expression = "java(domain.longitude())")
    @Mapping(target = "projectSize", expression = "java(domain.projectSize().value())")
    @Mapping(target = "budget", expression = "java(domain.budget().value())")

    @Mapping(target = "estimatedEnergy", expression = "java(domain.energyOutput().kwhPerYear())")
    @Mapping(target = "co2Reduction", expression = "java(domain.co2Reduction().tonsPerYear())")
    @Mapping(target = "climateData", expression = "java(writeClimateData(domain.climateData()))")
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

    default double firstNonNull(Double primary, double defaultValue) {
        return primary != null ? primary : defaultValue;
    }

    default ClimateData parseClimateData(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(raw, ClimateData.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot parse climate_data JSON", ex);
        }
    }

    default String writeClimateData(ClimateData climateData) {
        if (climateData == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(climateData);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot write climate_data JSON", ex);
        }
    }

    List<Simulation> toDomainList(List<SimulationEntity> entities);
}
