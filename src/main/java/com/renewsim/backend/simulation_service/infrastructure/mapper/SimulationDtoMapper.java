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
    SimulationCreationResultDTO toCreationResult(Simulation domain);

    SimulationUpdateResultDTO toUpdateResult(Simulation domain);

    SimulationQueryResultDTO toQueryResult(Simulation domain);

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

