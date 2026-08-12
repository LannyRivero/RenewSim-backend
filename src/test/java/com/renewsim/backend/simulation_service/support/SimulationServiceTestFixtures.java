package com.renewsim.backend.simulation_service.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.create.application.SimulationEngine;
import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.port.out.PvgisSolarResourcePort;
import com.renewsim.backend.simulation_service.create.application.technology.hydro.HydroSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.technology.solar.SolarSimulationAssessmentPolicy;
import com.renewsim.backend.simulation_service.create.application.technology.solar.SolarSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.technology.wind.WindSimulationEngine;
import com.renewsim.backend.simulation_service.dashboard.application.PortfolioScenarioScoringPolicy;
import com.renewsim.backend.simulation_service.dashboard.application.ScenarioSnapshotAssembler;
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
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;

import java.time.LocalDateTime;
import java.util.List;

public final class SimulationServiceTestFixtures {

        private SimulationServiceTestFixtures() {
        }

        public static CreateRealSimulationCommand validCommand() {
                return new CreateRealSimulationCommand(
                                "Solar - Sevilla",
                                Technology.solar(),
                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain",
                                                CountryCode.of("ES")),
                                new SimulationSystem(300, 0.81, 0.5, 99, new SimulationSystem.LossesPct(2, 6, 1, 3, 1)),
                                ConsumptionProfile.of(120000,
                                                List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                                                10000d, 10000d, 10000d, 10000d)),
                                new SimulationEconomics(Currency.of("EUR"), 315000, 7200, 0.18, 0.07, 8,
                                                ProjectLifetime.of(20)),
                                List.of(),
                                null,
                                "alice");
        }

        public static List<SimulationEngine> engines(PvgisSolarResourcePort resourcePort) {
                return List.of(
                                new SolarSimulationEngine(resourcePort, new SolarSimulationAssessmentPolicy()),
                                new WindSimulationEngine(),
                                new HydroSimulationEngine());
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

        public static PvgisSolarResourcePort.PvgisSolarResourceProfile profile() {
                return new PvgisSolarResourcePort.PvgisSolarResourceProfile(
                                List.of(117.91, 117.78, 140.16, 142.40, 155.64, 154.71, 164.37, 161.96, 145.57, 132.00,
                                                112.14, 112.69),
                                List.of(144.21, 146.57, 177.98, 185.86, 208.58, 212.34, 230.04, 226.42, 197.02, 172.86,
                                                140.05, 137.50),
                                List.of(10.0, 12.0, 15.0, 17.0, 22.0, 27.0, 31.0, 31.0, 27.0, 21.0, 15.0, 11.0),
                                "2005-2020",
                                "PVGIS");
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

        public static SimulationDetailsResult resultWithMetrics(
                        String id,
                        String recommendation,
                        double annualSavings,
                        double netAnnualBenefit,
                        double paybackYears,
                        double specificYield,
                        List<SimulationDetailsResult.SimulationWarning> warnings) {
                return new SimulationDetailsResult(
                                id,
                                "completed",
                                "2026-06-30T14:00:00Z",
                                "2026-06-30T14:00:00Z",
                                "solar-spain-v1",
                                "solar",
                                new SimulationDetailsResult.ResolvedLocation("Sevilla, Andalucia, ES", "Sevilla",
                                                "Andalucia", "Spain", "ES", 37.3891, -5.9845, "Europe/Madrid"),
                                new SimulationDetailsResult.Summary(recommendation, "headline", "summary",
                                                List.of(new SimulationDetailsResult.RecommendationReason("economics",
                                                                "positive", "Driver"))),
                                new SimulationDetailsResult.Input("Solar - Sevilla", "solar",
                                                new SimulationDetailsResult.Location("Sevilla, Andalucia, ES", 37.3891,
                                                                -5.9845, "Spain", "ES"),
                                                new SimulationDetailsResult.SystemSpec(300, 0.81, 0.5, 99,
                                                                new SimulationDetailsResult.LossesPct(2, 6, 1, 3, 1)),
                                                new SimulationDetailsResult.Demand(120000, List.of()),
                                                new SimulationDetailsResult.Economics("EUR", 315000, 7200, 0.18, 0.07,
                                                                8, 20)),
                                new SimulationDetailsResult.Technical(457200, List.of(), specificYield, 0.81, 17.4,
                                                72.3, 31.5,
                                                new SimulationDetailsResult.ResourceSeries("PVGIS", "2005-2020",
                                                                List.of(), List.of()),
                                                new SimulationDetailsResult.LossesSummary(2, 6, 1, 3, 1, 13),
                                                List.of()),
                                new SimulationDetailsResult.Financial("EUR", annualSavings, 8800, netAnnualBenefit,
                                                paybackYears, 8.7, 121500, 11.4, 0.071, List.of()),
                                new SimulationDetailsResult.Assumptions(8, 20, 0.5, 0.18, 0.07, "PVGIS", "2005-2020"),
                                warnings);
        }

        public static SimulationResultSnapshotReaderPort snapshotReader() {
                return new SimulationResultSnapshotJacksonReader(new ObjectMapper().findAndRegisterModules());
        }

        public static ScenarioSnapshotAssembler snapshotAssembler(TechnologyLookupPort technologyLookupPort) {
                return new ScenarioSnapshotAssembler(
                                technologyLookupPort,
                                snapshotReader(),
                                new PortfolioScenarioScoringPolicy());
        }
}
