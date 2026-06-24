package com.renewsim.backend.simulation_service.infrastructure.mapper;

import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.Budget;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectSize;
import com.renewsim.backend.simulation_service.infrastructure.persistence.entity.SimulationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationMapperTest {

    private final SimulationMapper mapper = Mappers.getMapper(SimulationMapper.class);

    @Test
    @DisplayName("toDomain should use baseline columns when present")
    void toDomainUsesBaselineColumns() {
        SimulationEntity entity = SimulationEntity.builder()
                .id(10L)
                .location("Santiago")
                .energyType("SOLAR")
                .projectSize(120.0)
                .budget(50000.0)
                .estimatedEnergy(1234.5)
                .co2Reduction(88.0)
                .createdBy("alice")
                .createdAt(LocalDateTime.parse("2026-05-20T10:15:30"))
                .technologyIds(List.of(2L, 9L))
                .build();

        entity.setLocationName("Legacy Name");
        entity.setCapacityKw(20.0);
        entity.setInitialInvestment(99.0);
        entity.setEnergyGenerated(111.0);

        Simulation result = mapper.toDomain(entity);

        assertThat(result.name()).isEqualTo("Simulation 10");
        assertThat(result.location()).isEqualTo("Santiago");
        assertThat(result.projectSize().value()).isEqualTo(120.0);
        assertThat(result.budget().value()).isEqualTo(50000.0);
        assertThat(result.energyOutput().kwhPerYear()).isEqualTo(1234.5);
        assertThat(result.createdBy()).isEqualTo("alice");
        assertThat(result.technologyIds()).containsExactly(2L, 9L);
    }

    @Test
    @DisplayName("toDomain should fallback to legacy V4 columns when baseline is missing")
    void toDomainFallsBackToLegacyColumns() {
        SimulationEntity entity = SimulationEntity.builder()
                .id(11L)
                .energyType("WIND")
                .co2Reduction(12.5)
                .createdAt(LocalDateTime.parse("2026-05-21T08:00:00"))
                .technologyIds(null)
                .build();

        entity.setLocationName("Legacy Location");
        entity.setCapacityKw(75.0);
        entity.setInitialInvestment(30000.0);
        entity.setEnergyGenerated(456.7);

        Simulation result = mapper.toDomain(entity);

        assertThat(result.name()).isEqualTo("Simulation 11");
        assertThat(result.location()).isEqualTo("Legacy Location");
        assertThat(result.projectSize().value()).isEqualTo(75.0);
        assertThat(result.budget().value()).isEqualTo(30000.0);
        assertThat(result.energyOutput().kwhPerYear()).isEqualTo(456.7);
        assertThat(result.createdBy()).isEqualTo("system");
        assertThat(result.technologyIds()).isEmpty();
    }

    @Test
    @DisplayName("toEntity should keep writing the baseline contract fields")
    void toEntityWritesBaselineContractFields() {
        Simulation domain = Simulation.reconstitute(
                12L,
                "Hydro Mendoza",
                "Mendoza",
                -32.8895,
                -68.8458,
                EnergyType.HYDRO,
                new ProjectSize(42.0),
                new Budget(70000.0),
                new EnergyOutput(1500.0),
                new CO2Reduction(44.5),
                new ClimateData(1, 2, 3),
                List.of(1L, 5L),
                "bob",
                LocalDateTime.parse("2026-05-22T09:10:11"));

        SimulationEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(12L);
        assertThat(entity.getName()).isEqualTo("Hydro Mendoza");
        assertThat(entity.getLocation()).isEqualTo("Mendoza");
        assertThat(entity.getLocationLat()).isEqualTo(-32.8895);
        assertThat(entity.getLocationLng()).isEqualTo(-68.8458);
        assertThat(entity.getEnergyType()).isEqualTo("HYDRO");
        assertThat(entity.getProjectSize()).isEqualTo(42.0);
        assertThat(entity.getBudget()).isEqualTo(70000.0);
        assertThat(entity.getEstimatedEnergy()).isEqualTo(1500.0);
        assertThat(entity.getCo2Reduction()).isEqualTo(44.5);
        assertThat(entity.getCreatedBy()).isEqualTo("bob");
        assertThat(entity.getTechnologyIds()).containsExactly(1L, 5L);
    }
}
