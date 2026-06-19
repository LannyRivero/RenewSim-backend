package com.renewsim.backend.simulation_service.infrastructure.mapper;

import com.renewsim.backend.simulation_service.application.result.*;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.*;
import org.mapstruct.*;

/**
 * ✅ SimulationDtoMapper
 *
 * Converts immutable domain models into immutable ResultDTOs used by the Application layer.
 * Explicitly flattens Value Objects (VOs) into primitives for serialization and transport.
 *
 * Notes:
 * - Compatible with Java records (ADR-003).
 * - Avoids Lombok and mutable structures.
 */
@Mapper(
    componentModel = "spring",
    implementationName = "SimulationDtoMapperImpl",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SimulationDtoMapper {

    // ============================================================
    // DOMAIN → RESULT DTO
    // ============================================================
    default SimulationCreationResultDTO toCreationResult(Simulation domain) {
        if (domain == null) {
            return null;
        }
        return new SimulationCreationResultDTO(
                domain.id(),
                domain.name(),
                domain.createdAt());
    }

    default SimulationUpdateResultDTO toUpdateResult(Simulation domain) {
        if (domain == null) {
            return null;
        }
        return new SimulationUpdateResultDTO(
                domain.id(),
                domain.location(),
                map(domain.energyType()),
                map(domain.projectSize()),
                map(domain.budget()),
                map(domain.energyOutput()),
                map(domain.co2Reduction()),
                domain.createdAt());
    }

    default SimulationQueryResultDTO toQueryResult(Simulation domain) {
        if (domain == null) {
            return null;
        }
        return new SimulationQueryResultDTO(
                domain.id(),
                domain.location(),
                map(domain.energyType()),
                map(domain.projectSize()),
                map(domain.budget()),
                map(domain.energyOutput()),
                map(domain.co2Reduction()),
                domain.createdAt(),
                domain.technologyIds() == null ? java.util.List.of() : domain.technologyIds().stream().map(String::valueOf).toList(),
                domain.createdBy());
    }

    // ============================================================
    // VALUE OBJECT MAPPERS
    // ============================================================
    default double map(ProjectSize value) {
        return value != null ? value.value() : 0.0;
    }

    default double map(Budget value) {
        return value != null ? value.value() : 0.0;
    }

    default double map(EnergyOutput value) {
        return value != null ? value.kwhPerYear() : 0.0;
    }

    default double map(CO2Reduction value) {
        return value != null ? value.tonsPerYear() : 0.0;
    }

    default String map(EnergyType value) {
        return value != null ? value.name() : null;
    }
}

