package com.renewsim.backend.technology_service.application.mapper;

import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.CapacityFactor;
import com.renewsim.backend.technology_service.domain.model.vo.Co2Reduction;
import com.renewsim.backend.technology_service.domain.model.vo.Efficiency;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;
import com.renewsim.backend.technology_service.domain.model.vo.EnvironmentalImpact;
import com.renewsim.backend.technology_service.domain.model.vo.InstallationCost;
import com.renewsim.backend.technology_service.domain.model.vo.MaintenanceCost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologyDtoMapperTest {

    private final TechnologyDtoMapper mapper = Mappers.getMapper(TechnologyDtoMapper.class);

    private Technology technology() {
        return new Technology(
                7L,
                "Wind Turbine",
                EnergyType.WIND,
                new Efficiency(35.0),
                new InstallationCost(BigDecimal.valueOf(2000)),
                30,
                new MaintenanceCost(BigDecimal.valueOf(120)),
                "Seeded description",
                true,
                Instant.parse("2026-05-10T08:00:00Z"),
                Instant.parse("2026-05-11T09:30:00Z"),
                new EnvironmentalImpact(8.0),
                new Co2Reduction(BigDecimal.valueOf(300)),
                new CapacityFactor(35.0));
    }

    @Test
    @DisplayName("should map domain technology to creation result")
    void shouldMapDomainTechnologyToCreationResult() {
        var result = mapper.toCreationResult(technology());

        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.name()).isEqualTo("Wind Turbine");
        assertThat(result.energyType()).isEqualTo("WIND");
        assertThat(result.efficiency()).isEqualTo(35.0);
        assertThat(result.environmentalImpact()).isEqualTo(8.0);
        assertThat(result.co2Reduction()).isEqualTo(300.0);
    }

    @Test
    @DisplayName("should map domain technology to update and query results")
    void shouldMapDomainTechnologyToUpdateAndQueryResults() {
        var update = mapper.toUpdateResult(technology());
        var query = mapper.toQueryResult(technology());

        assertThat(update.id()).isEqualTo(7L);
        assertThat(update.capacityFactor()).isEqualTo(35.0);
        assertThat(query.id()).isEqualTo(7L);
        assertThat(query.installationCost()).isEqualTo(2000.0);
        assertThat(query.maintenanceCost()).isEqualTo(120.0);
    }

    @Test
    @DisplayName("should map domain technology to response dto")
    void shouldMapDomainTechnologyToResponseDto() {
        var response = mapper.toResponse(technology());

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.description()).isEqualTo("Seeded description");
        assertThat(response.isActive()).isTrue();
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-05-10T08:00:00Z"));
        assertThat(response.updatedAt()).isEqualTo(Instant.parse("2026-05-11T09:30:00Z"));
    }

    @Test
    @DisplayName("should map null value objects to safe primitive defaults")
    void shouldMapNullValueObjectsToSafePrimitiveDefaults() {
        assertThat(mapper.map((Efficiency) null)).isEqualTo(0.0);
        assertThat(mapper.map((EnvironmentalImpact) null)).isEqualTo(0.0);
        assertThat(mapper.map((Co2Reduction) null)).isEqualTo(0.0);
        assertThat(mapper.map((CapacityFactor) null)).isEqualTo(0.0);
        assertThat(mapper.map((InstallationCost) null)).isEqualTo(0.0);
        assertThat(mapper.map((MaintenanceCost) null)).isEqualTo(0.0);
        assertThat(mapper.map((EnergyType) null)).isNull();
    }
}
