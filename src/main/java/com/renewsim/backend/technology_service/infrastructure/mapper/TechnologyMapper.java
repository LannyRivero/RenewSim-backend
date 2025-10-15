package com.renewsim.backend.technology_service.infrastructure.mapper;

import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import org.mapstruct.*;

import java.math.BigDecimal;

/**
 * ✅ TechnologyMapper
 *
 * Converts between domain aggregates (Technology) and persistence entities
 * (TechnologyEntity).
 * This mapper supports full VO <-> primitive transformations.
 *
 * Design notes:
 * - Uses explicit expressions to unwrap or rewrap Value Objects.
 * - Fully compatible with Java records and ADR-003 principles.
 * - Avoids Lombok annotations and relies on MapStruct for generation.
 */
@Mapper(componentModel = "spring", implementationName = "TechnologyMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {
        BigDecimal.class,
        Efficiency.class,
        InstallationCost.class,
        MaintenanceCost.class,
        EnvironmentalImpact.class,
        Co2Reduction.class,
        EnergyProduction.class,
        EnergyType.class
})
public interface TechnologyMapper {

    // ------------------------------------------------------------
    // ENTITY → DOMAIN
    // ------------------------------------------------------------
    @Mapping(target = "efficiency", expression = "java(new Efficiency(entity.getEfficiency()))")
    @Mapping(target = "installationCost", expression = "java(new InstallationCost(BigDecimal.valueOf(entity.getInstallationCost())))")
    @Mapping(target = "maintenanceCost", expression = "java(new MaintenanceCost(BigDecimal.valueOf(entity.getMaintenanceCost())))")
    @Mapping(target = "environmentalImpact", expression = "java(new EnvironmentalImpact(entity.getEnvironmentalImpact()))")
    @Mapping(target = "co2Reduction", expression = "java(new Co2Reduction(BigDecimal.valueOf(entity.getCo2Reduction())))")
    @Mapping(target = "energyProduction", expression = "java(new EnergyProduction(entity.getEnergyProduction()))")
    @Mapping(target = "energyType", expression = "java(EnergyType.valueOf(entity.getEnergyType().toUpperCase()))")
    Technology toDomain(TechnologyEntity entity);

    // ------------------------------------------------------------
    // DOMAIN → ENTITY
    // ------------------------------------------------------------
    @Mapping(target = "efficiency", expression = "java(domain.getEfficiency().value())")
    @Mapping(target = "installationCost", expression = "java(domain.getInstallationCost().value().doubleValue())")
    @Mapping(target = "maintenanceCost", expression = "java(domain.getMaintenanceCost().value().doubleValue())")
    @Mapping(target = "environmentalImpact", expression = "java(domain.getEnvironmentalImpact().value())")
    @Mapping(target = "co2Reduction", expression = "java(domain.getCo2Reduction().value().doubleValue())")
    @Mapping(target = "energyProduction", expression = "java(domain.getEnergyProduction().value())")
    @Mapping(target = "energyType", expression = "java(domain.getEnergyType().name())")
    TechnologyEntity toEntity(Technology domain);
}
