package com.renewsim.backend.technology_service.infraestructure.mapper;

import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.infrastructure.mapper.TechnologyMapper;
import com.renewsim.backend.technology_service.infrastructure.mapper.TechnologyMapperImpl;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link TechnologyMapper} (MapStruct auto-generated implementation).
 * Ensures accurate bidirectional conversion between Domain and Entity layers.
 */
class TechnologyMapperTest extends BaseValueObjectTest {

    private final TechnologyMapper mapper = new TechnologyMapperImpl();

    @Test
    @DisplayName("Should map domain ↔ entity preserving all attributes")
    void shouldMapDomainToEntityAndBack() {
        // ─────────────────────────────
        // ARRANGE
        // ─────────────────────────────
        Technology domain = new Technology(
                "Solar Panel",
                EnergyType.SOLAR,
                new Efficiency(85.0),
                new InstallationCost(BigDecimal.valueOf(12000)),
                new MaintenanceCost(BigDecimal.valueOf(400)),
                new EnvironmentalImpact(5.0),
                new Co2Reduction(BigDecimal.valueOf(25.0)),
                new EnergyProduction(3000.0)
        );

        // ─────────────────────────────
        // ACT (Domain → Entity)
        // ─────────────────────────────
        TechnologyEntity entity = mapper.toEntity(domain);

        // ─────────────────────────────
        // ASSERT (Entity fields)
        // ─────────────────────────────
        assertNotNull(entity, "Entity should not be null");
        assertEquals("Solar Panel", entity.getName());
        assertEquals("SOLAR", entity.getEnergyType());
        assertEquals(85.0, entity.getEfficiency(), 0.001);
        assertEquals(12000.0, entity.getInstallationCost(), 0.001);
        assertEquals(400.0, entity.getMaintenanceCost(), 0.001);
        assertEquals(5.0, entity.getEnvironmentalImpact(), 0.001);
        assertEquals(25.0, entity.getCo2Reduction(), 0.001);
        assertEquals(3000.0, entity.getEnergyProduction(), 0.001);

        // ─────────────────────────────
        // ACT (Entity → Domain)
        // ─────────────────────────────
        Technology result = mapper.toDomain(entity);

        // ─────────────────────────────
        // ASSERT (Roundtrip consistency)
        // ─────────────────────────────
        assertNotNull(result, "Mapped domain object should not be null");
        assertEquals(domain.getName(), result.getName());
        assertEquals(domain.getEnergyType(), result.getEnergyType());
        assertEquals(domain.getEfficiency().value(), result.getEfficiency().value(), 0.001);
        assertBigDecimalEquals(domain.getInstallationCost().value(), result.getInstallationCost().value());
        assertBigDecimalEquals(domain.getMaintenanceCost().value(), result.getMaintenanceCost().value());
        assertEquals(domain.getEnvironmentalImpact().value(), result.getEnvironmentalImpact().value(), 0.001);
        assertBigDecimalEquals(domain.getCo2Reduction().value(), result.getCo2Reduction().value());
        assertEquals(domain.getEnergyProduction().value(), result.getEnergyProduction().value(), 0.001);
    }
}
