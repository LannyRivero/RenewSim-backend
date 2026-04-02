package com.renewsim.backend.technology_service.infrastructure.mapper;

import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import org.mapstruct.*;

import java.math.BigDecimal;

/**
 * 🧭 TechnologyMapper 
 *
 * ✅ Responsabilidad: traducir entre entidades de persistencia y modelos de dominio inmutables.
 * ✅ Diseñado para Clean Architecture, DDD y compatibilidad con MapStruct 1.6.2 / Java 21.
 *
 * 💡 Notas:
 * - Implementa conversión explícita para evitar reflección innecesaria.
 * - Usa métodos helper para mayor claridad y reusabilidad.
 * - Marcado con @Mapper para generación automática del lado ENTITY → DTO.
 * - El método toDomain() se define manualmente para preservar inmutabilidad del Domain Model.
 */
@Mapper(
    componentModel = "spring",
    implementationName = "TechnologyMapperImpl",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TechnologyMapper {

    // ============================================================
    // ✅ ENTITY → DOMAIN
    // ============================================================

    /**
     * Convierte una entidad JPA en un agregado de dominio inmutable.
     * Se hace manualmente por las restricciones del constructor del modelo.
     */
    default Technology toDomain(TechnologyEntity entity) {
        if (entity == null) return null;

        return new Technology(
            entity.getId(),
            entity.getName(),
            parseEnergyType(entity.getEnergyType()),
            new Efficiency(entity.getEfficiency().doubleValue()),
            new InstallationCost(entity.getUnitCost()),
            new MaintenanceCost(entity.getMaintenanceCost()),
            new EnvironmentalImpact(entity.getCo2ReductionFactor().doubleValue()),
            new Co2Reduction(entity.getCo2ReductionFactor()),
            new EnergyProduction(entity.getCapacityFactor().doubleValue())
        );
    }

    // ============================================================
    // ✅ DOMAIN → ENTITY (MapStruct handled)
    // ============================================================

    @Mapping(target = "energyType", expression = "java(TechnologyEntity.EnergyType.valueOf(domain.getEnergyType().name()))")
    @Mapping(target = "efficiency", expression = "java(BigDecimal.valueOf(domain.getEfficiency().value()))")
    @Mapping(target = "unitCost", expression = "java(domain.getInstallationCost().value())")
    @Mapping(target = "maintenanceCost", expression = "java(domain.getMaintenanceCost().value())")
    @Mapping(target = "co2ReductionFactor", expression = "java(domain.getCo2Reduction().value())")
    @Mapping(target = "capacityFactor", expression = "java(BigDecimal.valueOf(domain.getEnergyProduction().value()))")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "lifespanYears", constant = "25")
    @Mapping(target = "minCapacityKw", constant = "0.00")
    @Mapping(target = "maxCapacityKw", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TechnologyEntity toEntity(Technology domain);

    // ============================================================
    // 🧩 Helper Methods
    // ============================================================

    /**
     * Convierte un tipo de energía en su enum correspondiente,
     * asegurando robustez ante mayúsculas/minúsculas.
     */
    default EnergyType parseEnergyType(TechnologyEntity.EnergyType energyType) {
        if (energyType == null) {
            return EnergyType.SOLAR;
        }
        try {
            return EnergyType.valueOf(energyType.name());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Unknown energy type: " + energyType, ex);
        }
    }
}