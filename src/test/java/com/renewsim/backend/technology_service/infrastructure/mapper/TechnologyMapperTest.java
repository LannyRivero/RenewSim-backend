package com.renewsim.backend.technology_service.infrastructure.mapper;

import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.CapacityFactor;
import com.renewsim.backend.technology_service.domain.model.vo.Co2Reduction;
import com.renewsim.backend.technology_service.domain.model.vo.Efficiency;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;
import com.renewsim.backend.technology_service.domain.model.vo.EnvironmentalImpact;
import com.renewsim.backend.technology_service.domain.model.vo.InstallationCost;
import com.renewsim.backend.technology_service.domain.model.vo.MaintenanceCost;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologyMapperTest {

    private final TechnologyMapper mapper = Mappers.getMapper(TechnologyMapper.class);

    @Test
    @DisplayName("toEntity should preserve persisted metadata needed by update and soft delete")
    void toEntityShouldPreservePersistedMetadataNeededByUpdateAndSoftDelete() {
        Technology technology = new Technology(
                7L,
                "Wind Turbine",
                EnergyType.WIND,
                new Efficiency(0.35),
                new InstallationCost(BigDecimal.valueOf(2000)),
                30,
                new MaintenanceCost(BigDecimal.valueOf(120)),
                "Seeded description",
                false,
                Instant.parse("2026-05-10T08:00:00Z"),
                Instant.parse("2026-05-11T09:30:00Z"),
                new EnvironmentalImpact(8.0),
                new Co2Reduction(BigDecimal.valueOf(300)),
                new CapacityFactor(35.0));

        TechnologyEntity entity = mapper.toEntity(technology);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getLifespanYears()).isEqualTo(30);
        assertThat(entity.getDescription()).isEqualTo("Seeded description");
        assertThat(entity.getIsActive()).isFalse();
        assertThat(entity.getCreatedAt()).isEqualTo(Instant.parse("2026-05-10T08:00:00Z"));
        assertThat(entity.getUpdatedAt()).isEqualTo(Instant.parse("2026-05-11T09:30:00Z"));
    }
}
