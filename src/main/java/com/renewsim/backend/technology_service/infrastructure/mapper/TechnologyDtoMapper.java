package com.renewsim.backend.technology_service.infrastructure.mapper;

import com.renewsim.backend.technology_service.application.result.*;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for converting Technology domain models into Data Transfer Objects (DTOs).
 * Handles explicit transformation of complex Value Objects (VOs) into primitive representations.
 */
@Mapper(
        componentModel = "spring",
        implementationName = "TechnologyDtoMapperImpl",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TechnologyDtoMapper {

    // --- Core Mappings ---
    TechnologyCreationResultDTO toCreationResult(Technology domain);
    TechnologyUpdateResultDTO toUpdateResult(Technology domain);
    TechnologyQueryResultDTO toQueryResult(Technology domain);

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

    default double map(EnergyProduction value) {
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
