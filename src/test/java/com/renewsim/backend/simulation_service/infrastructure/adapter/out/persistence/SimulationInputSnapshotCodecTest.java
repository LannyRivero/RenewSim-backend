package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationInputSnapshotCodec.SimulationInputData;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationInputSnapshotCodecTest {

  private final SimulationInputSnapshotCodec codec = new SimulationInputSnapshotCodec(
      new ObjectMapper().findAndRegisterModules());

  @Test
  @DisplayName("readNormalized fills missing historical fields with safe defaults")
  void readNormalizedFillsMissingHistoricalFieldsWithSafeDefaults() {
    SimulationEntity entity = new SimulationEntity();
    entity.setLocation("Sevilla, Andalucia, ES");
    entity.setEstimatedEnergy(24000.0);

    SimulationInputData normalized = codec.readNormalized("""
        {
          "locationCountry": "",
          "locationCountryCode": "",
          "performanceRatio": 0.0,
          "degradationRateAnnualPct": -1.0,
          "availabilityPct": 0.0,
          "lossesInverter": -2.0,
          "lossesTemperature": -3.0,
          "lossesWiring": -4.0,
          "lossesSoiling": -5.0,
          "lossesOther": -6.0,
          "annualConsumptionKwh": 0.0,
          "monthlyConsumptionKwh": [],
          "currency": "",
          "opexAnnual": -10.0,
          "electricityPurchasePricePerKwh": -0.3,
          "exportPricePerKwh": -0.2,
          "discountRatePct": -1.0,
          "projectLifetimeYears": 1
        }
        """, entity);

    assertThat(normalized.locationCountry()).isEqualTo("Spain");
    assertThat(normalized.locationCountryCode()).isEqualTo("ES");
    assertThat(normalized.performanceRatio()).isEqualTo(0.81);
    assertThat(normalized.degradationRateAnnualPct()).isEqualTo(0.5);
    assertThat(normalized.availabilityPct()).isEqualTo(99.0);
    assertThat(normalized.lossesInverter()).isZero();
    assertThat(normalized.annualConsumptionKwh()).isEqualTo(24000.0);
    assertThat(normalized.monthlyConsumptionKwh()).hasSize(12);
    assertThat(normalized.monthlyConsumptionKwh()).allMatch(value -> value == 2000.0);
    assertThat(normalized.currency()).isEqualTo("EUR");
    assertThat(normalized.projectLifetimeYears()).isEqualTo(20);
  }

  @Test
  @DisplayName("readNormalized preserves complete snapshot data")
  void readNormalizedPreservesCompleteSnapshotData() {
    SimulationEntity entity = new SimulationEntity();
    entity.setLocation("Cordoba, Andalucia, ES");
    entity.setEstimatedEnergy(1000.0);

    SimulationInputData normalized = codec.readNormalized("""
        {
          "locationCountry": "Argentina",
          "locationCountryCode": "AR",
          "performanceRatio": 0.79,
          "degradationRateAnnualPct": 0.7,
          "availabilityPct": 98.0,
          "lossesInverter": 2.0,
          "lossesTemperature": 6.0,
          "lossesWiring": 1.0,
          "lossesSoiling": 3.0,
          "lossesOther": 1.0,
          "annualConsumptionKwh": 12000.0,
          "monthlyConsumptionKwh": [1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000],
          "currency": "USD",
          "opexAnnual": 200.0,
          "electricityPurchasePricePerKwh": 0.21,
          "exportPricePerKwh": 0.09,
          "discountRatePct": 8.0,
          "projectLifetimeYears": 25
        }
        """, entity);

    assertThat(normalized.locationCountry()).isEqualTo("Argentina");
    assertThat(normalized.locationCountryCode()).isEqualTo("AR");
    assertThat(normalized.monthlyConsumptionKwh())
        .isEqualTo(List.of(1000d, 1000d, 1000d, 1000d, 1000d, 1000d, 1000d, 1000d, 1000d, 1000d, 1000d, 1000d));
    assertThat(normalized.currency()).isEqualTo("USD");
    assertThat(normalized.projectLifetimeYears()).isEqualTo(25);
  }
}
