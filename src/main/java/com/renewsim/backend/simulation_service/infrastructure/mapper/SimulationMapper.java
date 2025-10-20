package com.renewsim.backend.simulation_service.infrastructure.mapper;

import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.*;
import com.renewsim.backend.simulation_service.infrastructure.persistence.entity.SimulationEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * 🧭 SimulationMapper
 *
 * ✅ Responsibility: translate between persistence entities and immutable domain
 * models.
 * ✅ Designed for Clean Architecture & DDD compatibility.
 *
 * 💡 Notes:
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
        if (entity == null)
            return null;

        return new Simulation(
                entity.getId(),
                entity.getLocation(),
                parseEnergyType(entity.getEnergyType()),
                new ProjectSize(entity.getProjectSize()),
                new Budget(entity.getBudget()),
                new EnergyOutput(entity.getEstimatedEnergy()),
                new CO2Reduction(entity.getCo2Reduction()),
                new ClimateData(0, 0, 0), // Climate data not persisted
                entity.getTechnologyIds(),
                entity.getCreatedAt());
    }

    // ============================================================
    // DOMAIN → ENTITY (MapStruct handled)
    // ============================================================
    @Mapping(target = "energyType", expression = "java(domain.energyType().name())")
    @Mapping(target = "projectSize", expression = "java(domain.projectSize().value())")
    @Mapping(target = "budget", expression = "java(domain.budget().value())")
    @Mapping(target = "estimatedEnergy", expression = "java(domain.energyOutput().kwhPerYear())")
    @Mapping(target = "co2Reduction", expression = "java(domain.co2Reduction().tonsPerYear())")
    @Mapping(target = "technologyIds", expression = "java(domain.technologyIds())")
    @Mapping(target = "createdAt", expression = "java(domain.createdAt())")
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

    List<Simulation> toDomainList(List<SimulationEntity> entities);
}
