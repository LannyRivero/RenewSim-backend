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
            new Efficiency(entity.getEfficiency()),
            new InstallationCost(BigDecimal.valueOf(entity.getInstallationCost())),
            new MaintenanceCost(BigDecimal.valueOf(entity.getMaintenanceCost())),
            new EnvironmentalImpact(entity.getEnvironmentalImpact()),
            new Co2Reduction(BigDecimal.valueOf(entity.getCo2Reduction())),
            new EnergyProduction(entity.getEnergyProduction())
        );
    }

    // ============================================================
    // ✅ DOMAIN → ENTITY (MapStruct handled)
    // ============================================================

    @Mapping(target = "energyType", expression = "java(domain.getEnergyType().name())")
    @Mapping(target = "efficiency", expression = "java(domain.getEfficiency().value())")
    @Mapping(target = "installationCost", expression = "java(domain.getInstallationCost().value().doubleValue())")
    @Mapping(target = "maintenanceCost", expression = "java(domain.getMaintenanceCost().value().doubleValue())")
    @Mapping(target = "environmentalImpact", expression = "java(domain.getEnvironmentalImpact().value())")
    @Mapping(target = "co2Reduction", expression = "java(domain.getCo2Reduction().value().doubleValue())")
    @Mapping(target = "energyProduction", expression = "java(domain.getEnergyProduction().value())")
    TechnologyEntity toEntity(Technology domain);

    // ============================================================
    // 🧩 Helper Methods
    // ============================================================

    /**
     * Convierte un tipo de energía en su enum correspondiente,
     * asegurando robustez ante mayúsculas/minúsculas.
     */
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
}
