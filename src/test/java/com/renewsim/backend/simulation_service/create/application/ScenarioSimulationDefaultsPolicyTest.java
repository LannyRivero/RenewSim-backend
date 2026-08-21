package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationCurrencyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScenarioSimulationDefaultsPolicyTest {

    private final ScenarioSimulationDefaultsPolicy policy = new ScenarioSimulationDefaultsPolicy();

    @Test
    @DisplayName("buildSystem applies the explicit scenario default policy values")
    void buildSystemAppliesExplicitDefaults() {
        var system = policy.buildSystem(5.0);

        assertThat(system.installedCapacityKw()).isEqualTo(5.0);
        assertThat(system.performanceRatio()).isEqualTo(0.81);
        assertThat(system.degradationRateAnnualPct()).isEqualTo(0.5);
        assertThat(system.availabilityPct()).isEqualTo(99.0);
        assertThat(system.lossesPct().inverter()).isEqualTo(2.0);
        assertThat(system.lossesPct().temperature()).isEqualTo(6.0);
        assertThat(system.lossesPct().wiring()).isEqualTo(1.0);
        assertThat(system.lossesPct().soiling()).isEqualTo(3.0);
        assertThat(system.lossesPct().other()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("buildEconomics applies the explicit financial default policy values")
    void buildEconomicsAppliesExplicitDefaults() {
        var economics = policy.buildEconomics(12000.0, "EUR", 0.15);

        assertThat(economics.capexTotal()).isEqualTo(12000.0);
        assertThat(economics.currency().value()).isEqualTo("EUR");
        assertThat(economics.opexAnnual()).isEqualTo(0.0);
        assertThat(economics.electricityPurchasePricePerKwh()).isEqualTo(0.15);
        assertThat(economics.exportPricePerKwh()).isEqualTo(0.07);
        assertThat(economics.discountRatePct()).isEqualTo(8.0);
        assertThat(economics.projectLifetime().years()).isEqualTo(20);
    }

    @Test
    @DisplayName("buildEconomics rejects unsupported currencies through the policy")
    void buildEconomicsRejectsUnsupportedCurrencies() {
        assertThatThrownBy(() -> policy.buildEconomics(12000.0, "USD", 0.15))
                .isInstanceOf(InvalidSimulationCurrencyException.class)
                .hasMessage("VALIDATION_ERROR: scenario defaultInvestmentCurrency must be EUR");
    }
}
