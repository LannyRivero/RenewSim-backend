package com.renewsim.backend.scenario_service.domain.model;

import com.renewsim.backend.scenario_service.domain.exception.InvalidScenarioParameterException;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import com.renewsim.backend.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScenarioTest {

    @Test
    @DisplayName("should create an active scenario with valid values")
    void shouldCreateAnActiveScenarioWithValidValues() {
        Scenario scenario = new Scenario(
                "Residential Solar",
                "Default home solar setup",
                1L,
                5.0,
                new Money(new BigDecimal("7500.00"), "USD"),
                0.15,
                6000.0,
                new ClimateData(5.5, 3.2, 22.0));

        assertThat(scenario.getId()).isNull();
        assertThat(scenario.getName()).isEqualTo("Residential Solar");
        assertThat(scenario.getTechnologyId()).isEqualTo(1L);
        assertThat(scenario.getDefaultCapacityKw()).isEqualTo(5.0);
        assertThat(scenario.getDefaultInvestment().amount()).isEqualByComparingTo("7500.00");
        assertThat(scenario.getDefaultTariff()).isEqualTo(0.15);
        assertThat(scenario.getDefaultConsumption()).isEqualTo(6000.0);
        assertThat(scenario.isActive()).isTrue();
    }

    @Test
    @DisplayName("should deactivate scenario preserving its values")
    void shouldDeactivateScenarioPreservingItsValues() {
        Scenario scenario = new Scenario(
                10L,
                "Industrial Wind",
                "Default wind scenario",
                new com.renewsim.backend.scenario_service.domain.model.vo.ScenarioTechnologyId(2L),
                new com.renewsim.backend.scenario_service.domain.model.vo.DefaultCapacityKw(50.0),
                new Money(new BigDecimal("120000.00"), "USD"),
                new com.renewsim.backend.scenario_service.domain.model.vo.DefaultTariff(0.12),
                new com.renewsim.backend.scenario_service.domain.model.vo.DefaultConsumption(150000.0),
                new ClimateData(4.0, 9.5, 18.0),
                true);

        Scenario deactivated = scenario.deactivate();

        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.getId()).isEqualTo(10L);
        assertThat(deactivated.getName()).isEqualTo("Industrial Wind");
        assertThat(deactivated.getTechnologyId()).isEqualTo(2L);
        assertThat(deactivated.getClimateProfile()).isEqualTo(scenario.getClimateProfile());
    }

    @Test
    @DisplayName("should reject invalid scenario values")
    void shouldRejectInvalidScenarioValues() {
        ClimateData climate = new ClimateData(5.5, 3.2, 22.0);
        Money investment = new Money(new BigDecimal("7500.00"), "USD");

        assertThatThrownBy(() -> new Scenario("   ", null, 1L, 5.0, investment, 0.15, 6000.0, climate))
                .isInstanceOf(InvalidScenarioParameterException.class)
                .hasMessage("Scenario name cannot be null or blank");

        assertThatThrownBy(() -> new Scenario("Residential Solar", null, 0L, 5.0, investment, 0.15, 6000.0, climate))
                .isInstanceOf(InvalidScenarioParameterException.class)
                .hasMessage("Technology id must be greater than zero");

        assertThatThrownBy(() -> new Scenario("Residential Solar", null, 1L, 0.0, investment, 0.15, 6000.0, climate))
                .isInstanceOf(InvalidScenarioParameterException.class)
                .hasMessage("Default capacity must be greater than zero");

        assertThatThrownBy(() -> new Scenario("Residential Solar", null, 1L, 5.0, investment, -1.0, 6000.0, climate))
                .isInstanceOf(InvalidScenarioParameterException.class)
                .hasMessage("Default tariff cannot be negative");

        assertThatThrownBy(() -> new Scenario("Residential Solar", null, 1L, 5.0, investment, 0.15, -1.0, climate))
                .isInstanceOf(InvalidScenarioParameterException.class)
                .hasMessage("Default consumption cannot be negative");
    }
}
