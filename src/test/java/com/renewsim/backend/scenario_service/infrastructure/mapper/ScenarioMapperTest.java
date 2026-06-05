package com.renewsim.backend.scenario_service.infrastructure.mapper;

import com.renewsim.backend.scenario_service.domain.model.Scenario;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultCapacityKw;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultConsumption;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultTariff;
import com.renewsim.backend.scenario_service.domain.model.vo.ScenarioTechnologyId;
import com.renewsim.backend.scenario_service.infrastructure.persistence.entity.ScenarioEntity;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import com.renewsim.backend.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ScenarioMapperTest {

    private final ScenarioMapper mapper = new ScenarioMapper();

    @Test
    @DisplayName("toEntity should map scenario aggregate fields")
    void toEntityShouldMapScenarioAggregateFields() {
        Scenario scenario = new Scenario(
                1L,
                "Residential Solar",
                "Default home solar setup",
                new ScenarioTechnologyId(5L),
                new DefaultCapacityKw(5.0),
                new Money(new BigDecimal("7500.00"), "USD"),
                new DefaultTariff(0.15),
                new DefaultConsumption(6000.0),
                new ClimateData(5.5, 3.2, 22.0),
                true);

        ScenarioEntity entity = mapper.toEntity(scenario);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getTechnologyId()).isEqualTo(5L);
        assertThat(entity.getDefaultInvestmentAmount()).isEqualByComparingTo("7500.00");
        assertThat(entity.getDefaultInvestmentCurrency()).isEqualTo("USD");
        assertThat(entity.getClimateProfile()).isEqualTo(scenario.getClimateProfile());
        assertThat(entity.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("toDomain should rebuild scenario aggregate fields")
    void toDomainShouldRebuildScenarioAggregateFields() {
        ScenarioEntity entity = ScenarioEntity.builder()
                .id(2L)
                .name("Industrial Wind")
                .description("Default wind setup")
                .technologyId(9L)
                .defaultCapacityKw(new BigDecimal("50.00"))
                .defaultInvestmentAmount(new BigDecimal("120000.00"))
                .defaultInvestmentCurrency("USD")
                .defaultTariff(new BigDecimal("0.12"))
                .defaultConsumption(new BigDecimal("150000.00"))
                .climateProfile(new ClimateData(4.0, 9.5, 18.0))
                .isActive(false)
                .build();

        Scenario scenario = mapper.toDomain(entity);

        assertThat(scenario.getId()).isEqualTo(2L);
        assertThat(scenario.getName()).isEqualTo("Industrial Wind");
        assertThat(scenario.getTechnologyId()).isEqualTo(9L);
        assertThat(scenario.getDefaultInvestment().amount()).isEqualByComparingTo("120000.00");
        assertThat(scenario.getDefaultInvestment().currency()).isEqualTo("USD");
        assertThat(scenario.isActive()).isFalse();
    }
}
