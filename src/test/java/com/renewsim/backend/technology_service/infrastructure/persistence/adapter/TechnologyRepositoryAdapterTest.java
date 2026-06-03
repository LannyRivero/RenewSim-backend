package com.renewsim.backend.technology_service.infrastructure.persistence.adapter;

import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.CapacityFactor;
import com.renewsim.backend.technology_service.domain.model.vo.Co2Reduction;
import com.renewsim.backend.technology_service.domain.model.vo.Efficiency;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;
import com.renewsim.backend.technology_service.domain.model.vo.EnvironmentalImpact;
import com.renewsim.backend.technology_service.domain.model.vo.InstallationCost;
import com.renewsim.backend.technology_service.domain.model.vo.MaintenanceCost;
import com.renewsim.backend.technology_service.infrastructure.mapper.TechnologyMapper;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import com.renewsim.backend.technology_service.infrastructure.persistence.repository.JpaTechnologyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnologyRepositoryAdapterTest {

    @Mock
    private JpaTechnologyRepository jpaRepository;

    @Mock
    private TechnologyMapper mapper;

    @InjectMocks
    private TechnologyRepositoryAdapter adapter;

    @Test
    @DisplayName("save should preserve persistence-only min and max capacity fields on update")
    void saveShouldPreservePersistenceOnlyMinAndMaxCapacityFieldsOnUpdate() {
        Technology technology = new Technology(
                7L,
                "Wind Turbine",
                EnergyType.WIND,
                new Efficiency(0.35),
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

        TechnologyEntity mappedEntity = TechnologyEntity.builder()
                .id(7L)
                .name("Wind Turbine")
                .energyType(TechnologyEntity.EnergyType.WIND)
                .unitCost(BigDecimal.valueOf(2000))
                .maintenanceCost(BigDecimal.valueOf(120))
                .lifespanYears(30)
                .description("Seeded description")
                .efficiency(BigDecimal.valueOf(0.35))
                .capacityFactor(BigDecimal.valueOf(35.0))
                .co2ReductionFactor(BigDecimal.valueOf(300))
                .isActive(true)
                .createdAt(Instant.parse("2026-05-10T08:00:00Z"))
                .updatedAt(Instant.parse("2026-05-11T09:30:00Z"))
                .build();

        TechnologyEntity existingEntity = TechnologyEntity.builder()
                .id(7L)
                .minCapacityKw(BigDecimal.valueOf(5.50))
                .maxCapacityKw(BigDecimal.valueOf(55.50))
                .build();

        when(mapper.toEntity(technology)).thenReturn(mappedEntity);
        when(jpaRepository.findById(7L)).thenReturn(Optional.of(existingEntity));
        when(jpaRepository.save(mappedEntity)).thenReturn(mappedEntity);
        when(mapper.toDomain(mappedEntity)).thenReturn(technology);

        adapter.save(technology);

        ArgumentCaptor<TechnologyEntity> captor = ArgumentCaptor.forClass(TechnologyEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getMinCapacityKw()).isEqualByComparingTo("5.50");
        assertThat(captor.getValue().getMaxCapacityKw()).isEqualByComparingTo("55.50");
    }

    @Test
    @DisplayName("findActiveById should query only active technologies")
    void findActiveByIdShouldQueryOnlyActiveTechnologies() {
        TechnologyEntity entity = TechnologyEntity.builder().id(9L).build();
        Technology technology = new Technology(
                9L,
                "Solar Panel",
                EnergyType.SOLAR,
                new Efficiency(0.85),
                new InstallationCost(BigDecimal.valueOf(1200)),
                25,
                new MaintenanceCost(BigDecimal.valueOf(100)),
                null,
                true,
                null,
                null,
                new EnvironmentalImpact(10.0),
                new Co2Reduction(BigDecimal.valueOf(250)),
                new CapacityFactor(18.0));

        when(jpaRepository.findByIdAndIsActiveTrue(9L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(technology);

        var result = adapter.findActiveById(9L);

        assertThat(result).contains(technology);
        verify(jpaRepository).findByIdAndIsActiveTrue(9L);
    }
}
