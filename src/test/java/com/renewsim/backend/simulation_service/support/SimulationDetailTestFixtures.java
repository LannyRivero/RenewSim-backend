package com.renewsim.backend.simulation_service.support;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationResultSnapshotJacksonReader;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationResultSnapshotReaderPort;

import java.time.LocalDateTime;
import java.util.List;

public final class SimulationDetailTestFixtures {

        private SimulationDetailTestFixtures() {
        }

        public static Simulation completedSimulation(Long id, String owner, String resultJson) {
                SimulationLocation location = SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain",
                                CountryCode.of("ES"));
                SimulationSystem system = new SimulationSystem(300.0, 0.81, 0.5, 99.0,
                                new SimulationSystem.LossesPct(2.0, 6.0, 1.0, 3.0, 1.0));
                SimulationEconomics economics = new SimulationEconomics(Currency.of("EUR"), 315000.0, 7200.0, 0.18,
                                0.07, 8, ProjectLifetime.of(20));

                return Simulation.reconstitute(
                                id, "Solar - Sevilla", Technology.solar(),
                                location, system,
                                ConsumptionProfile.of(120000,
                                                List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                                                10000d, 10000d, 10000d, 10000d)),
                                economics,
                                SimulationStatus.COMPLETED, resultJson,
                                457200.0, 68700.0, 121500.0, 11.4, "viable_with_reservations",
                                List.of(11L, 12L),
                                null,
                                owner, LocalDateTime.parse("2026-06-30T14:00:00"),
                                LocalDateTime.parse("2026-06-30T14:00:00"));
        }

        public static SimulationDetailsResult minimalResult() {
                return new SimulationDetailsResult(
                                "55", "completed", "2026-06-30T14:00:00Z", "2026-06-30T14:00:00Z", "solar-spain-v1",
                                "solar",
                                new SimulationDetailsResult.ResolvedLocation("Sevilla, Andalucia, ES", "Sevilla",
                                                "Andalucia", "Spain", "ES", 37.3891, -5.9845, "Europe/Madrid"),
                                new SimulationDetailsResult.Summary("viable_with_reservations", "headline", "summary",
                                                List.of()),
                                new SimulationDetailsResult.Input("Solar - Sevilla", "solar",
                                                new SimulationDetailsResult.Location("Sevilla, Andalucia, ES", 37.3891,
                                                                -5.9845, "Spain", "ES"),
                                                new SimulationDetailsResult.SystemSpec(300, 0.81, 0.5, 99,
                                                                new SimulationDetailsResult.LossesPct(2, 6, 1, 3, 1)),
                                                new SimulationDetailsResult.Demand(120000, List.of()),
                                                new SimulationDetailsResult.Economics("EUR", 315000, 7200, 0.18, 0.07,
                                                                8, 20)),
                                new SimulationDetailsResult.Technical(457200, List.of(), 1524, 0.81, 17.4, 72.3, 31.5,
                                                new SimulationDetailsResult.ResourceSeries("PVGIS", "2005-2020",
                                                                List.of(), List.of()),
                                                new SimulationDetailsResult.LossesSummary(2, 6, 1, 3, 1, 13),
                                                List.of()),
                                new SimulationDetailsResult.Financial("EUR", 68700, 8800, 70300, 6.9, 8.7, 121500, 11.4,
                                                0.071, List.of()),
                                new SimulationDetailsResult.Assumptions(8, 20, 0.5, 0.18, 0.07, "PVGIS", "2005-2020"),
                                List.of());
        }

        public static SimulationResultSnapshotReaderPort snapshotReader() {
                return new SimulationResultSnapshotJacksonReader(new ObjectMapper().findAndRegisterModules());
        }
}
