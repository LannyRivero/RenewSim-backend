package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationStatus;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.infrastructure.persistence.entity.SimulationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationRecordEntityMapperTest {

    private final TechnologyLookupPort technologyLookupPort = new TechnologyLookupPort() {
        @Override
        public boolean existsActiveByEnergyType(String energyType) {
            return true;
        }

        @Override
        public Optional<Double> findActiveCo2ReductionFactorByEnergyType(String energyType) {
            return Optional.of(0.45);
        }
    };

    private final SimulationRecordEntityMapper mapper = new SimulationRecordEntityMapper(
            new SimulationInputSnapshotCodec(new ObjectMapper().findAndRegisterModules()),
            technologyLookupPort);

    @Test
    @DisplayName("toEntity persists aggregate values and computed co2 reduction")
    void toEntityPersistsAggregateValuesAndComputedCo2Reduction() {
        Simulation simulation = completedSimulation();

        SimulationEntity entity = mapper.toEntity(simulation);

        assertThat(entity.getId()).isEqualTo(55L);
        assertThat(entity.getEnergyType()).isEqualTo("solar");
        assertThat(entity.getEstimatedEnergy()).isEqualTo(457200.0);
        assertThat(entity.getCo2Reduction()).isEqualTo(205740.0);
        assertThat(entity.getStatus()).isEqualTo("COMPLETED");
        assertThat(entity.getInputSnapshot()).contains("\"locationCountry\":\"Spain\"");
    }

    @Test
    @DisplayName("toDomain rebuilds aggregate and defaults invalid status to draft")
    void toDomainRebuildsAggregateAndDefaultsInvalidStatusToDraft() {
        SimulationEntity entity = new SimulationEntity();
        entity.setId(88L);
        entity.setName("Solar - Historic");
        entity.setEnergyType("solar");
        entity.setLocation("Sevilla, Andalucia, ES");
        entity.setLocationLat(37.3891);
        entity.setLocationLng(-5.9845);
        entity.setProjectSize(300.0);
        entity.setBudget(315000.0);
        entity.setEstimatedEnergy(120000.0);
        entity.setCreatedBy("alice");
        entity.setCreatedAt(LocalDateTime.parse("2026-06-30T14:00:00"));
        entity.setUpdatedAt(LocalDateTime.parse("2026-06-30T14:30:00"));
        entity.setStatus("bogus");
        entity.setAnnualSavings(68700.0);
        entity.setNpv(121500.0);
        entity.setIrrPct(11.4);
        entity.setRecommendation("viable_with_reservations");
        entity.setResultSnapshot("{}");
        entity.setInputSnapshot("""
                {
                  "locationCountry": "Spain",
                  "locationCountryCode": "ES",
                  "performanceRatio": 0.81,
                  "degradationRateAnnualPct": 0.5,
                  "availabilityPct": 99.0,
                  "lossesInverter": 2.0,
                  "lossesTemperature": 6.0,
                  "lossesWiring": 1.0,
                  "lossesSoiling": 3.0,
                  "lossesOther": 1.0,
                  "annualConsumptionKwh": 120000.0,
                  "monthlyConsumptionKwh": [10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000],
                  "currency": "EUR",
                  "opexAnnual": 7200.0,
                  "electricityPurchasePricePerKwh": 0.18,
                  "exportPricePerKwh": 0.07,
                  "discountRatePct": 8.0,
                  "projectLifetimeYears": 20
                }
                """);

        Simulation simulation = mapper.toDomain(entity);

        assertThat(simulation.getId().value()).isEqualTo(88L);
        assertThat(simulation.getStatus()).isEqualTo(SimulationStatus.DRAFT);
        assertThat(simulation.getLocation().country()).isEqualTo("Spain");
        assertThat(simulation.getDemand().annualConsumptionKwh()).isEqualTo(120000.0);
        assertThat(simulation.getEconomics().capexTotal()).isEqualTo(315000.0);
        assertThat(simulation.getRecommendation()).isEqualTo("viable_with_reservations");
    }

    private Simulation completedSimulation() {
        return Simulation.reconstitute(
                55L,
                "Solar - Sevilla",
                Technology.solar(),
                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", CountryCode.of("ES")),
                new SimulationSystem(300.0, 0.81, 0.5, 99.0, new SimulationSystem.LossesPct(2.0, 6.0, 1.0, 3.0, 1.0)),
                ConsumptionProfile.of(120000,
                        List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                10000d)),
                new SimulationEconomics(Currency.of("EUR"), 315000.0, 7200.0, 0.18, 0.07, 8, ProjectLifetime.of(20)),
                SimulationStatus.COMPLETED,
                "{}",
                457200.0,
                68700.0,
                121500.0,
                11.4,
                "viable_with_reservations",
                "alice",
                LocalDateTime.parse("2026-06-30T14:00:00"),
                LocalDateTime.parse("2026-06-30T14:30:00"));
    }
}
