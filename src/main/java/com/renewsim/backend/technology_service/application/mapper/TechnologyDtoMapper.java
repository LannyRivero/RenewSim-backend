package com.renewsim.backend.technology_service.application.mapper;

import com.renewsim.backend.technology_service.application.result.*;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * ✅ TechnologyDtoMapper
 *
 * Converts domain models to immutable ResultDTOs used by the Application layer.
 * Implements explicit flattening of Value Objects (VOs) into primitives
 * for serialization and transport.
 *
 * Notes:
 * - Compatible with Java records (ADR-003).
 * - No Lombok or mutable classes required.
 */
@Mapper(componentModel = "spring", implementationName = "TechnologyDtoMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TechnologyDtoMapper {

    // ------------------------------------------------------------
    // DOMAIN → RESULT DTO
    // ------------------------------------------------------------
    TechnologyCreationResultDTO toCreationResult(Technology domain);

    TechnologyUpdateResultDTO toUpdateResult(Technology domain);

    TechnologyQueryResultDTO toQueryResult(Technology domain);

    TechnologyResponseDTO toResponse(Technology domain);

    // ------------------------------------------------------------
    // VALUE OBJECT MAPPERS
    // ------------------------------------------------------------
    default double map(Efficiency value) {
        return value != null ? value.value() : 0.0;
    }

    default double map(EnvironmentalImpact value) {
        return value != null ? value.value() : 0.0;
    }

    default double map(Co2Reduction value) {
        return value != null ? value.value().doubleValue() : 0.0;
    }

    default double map(CapacityFactor value) {
        return value != null ? value.value() : 0.0;
    }

    default double map(InstallationCost value) {
        return value != null ? value.value().doubleValue() : 0.0;
    }

    default double map(MaintenanceCost value) {
        return value != null ? value.value().doubleValue() : 0.0;
    }

    default String map(EnergyType value) {
        return value != null ? value.name() : null;
    }
}
