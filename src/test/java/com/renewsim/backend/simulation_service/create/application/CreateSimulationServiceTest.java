package com.renewsim.backend.simulation_service.create.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.shared.exception.BadRequestException;
import com.renewsim.backend.simulation_service.create.application.technology.hydro.HydroSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.technology.solar.SolarSimulationAssessmentPolicy;
import com.renewsim.backend.simulation_service.create.application.technology.solar.SolarSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.technology.wind.WindSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.dashboard.application.GetPortfolioDashboardService;
import com.renewsim.backend.simulation_service.dashboard.application.port.out.PortfolioDashboardQueryPort;
import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardRiskAlert;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardResult;
import com.renewsim.backend.simulation_service.detail.application.GetSimulationService;
import com.renewsim.backend.simulation_service.detail.application.port.out.SimulationDetailQueryPort;
import com.renewsim.backend.simulation_service.history.application.ListSimulationsService;
import com.renewsim.backend.simulation_service.history.application.port.out.SimulationHistoryQueryPort;
import com.renewsim.backend.simulation_service.history.application.result.UserSimulationListResult;
import com.renewsim.backend.simulation_service.create.application.port.out.PvgisSolarResourcePort;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationId;
import com.renewsim.backend.simulation_service.domain.model.SimulationStatus;
import com.renewsim.backend.simulation_service.domain.model.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RealSimulationServiceTest {

        private interface SimulationRepositoryPorts extends CreateSimulationRepositoryPort, SimulationDetailQueryPort,
                        SimulationHistoryQueryPort, PortfolioDashboardQueryPort {
        }

        @Mock
        private SimulationRepositoryPorts repository;
        @Mock
        private PvgisSolarResourcePort resourcePort;
        @Mock
        private TechnologyLookupPort technologyLookupPort;

        @Test
        @DisplayName("createSimulation computes and stores the real contract snapshots")
        void createSimulationComputesAndStoresRealContractSnapshots() {
                CreateSimulationService service = new CreateSimulationService(
                                repository,
                                technologyLookupPort,
                                engines(),
                                new SimulationCompletionMapper(new ObjectMapper().findAndRegisterModules()));
                CreateRealSimulationCommand command = validCommand();

                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.recommendActiveTechnologyIdsByEnergyType("solar"))
                                .thenReturn(List.of(1L, 2L));
                when(resourcePort.fetchProfile(37.3891, -5.9845, 13.0)).thenReturn(profile());
                when(repository.save(any())).thenAnswer(invocation -> {
                        Simulation sim = invocation.getArgument(0);
                        if (sim.getId() == null) {
                                sim.assignId(SimulationId.of(55L));
                        }
                        return sim;
                });

                SimulationDetailsResult response = service.createSimulation(command);

                assertThat(response.id()).isEqualTo("55");
                assertThat(response.modelVersion()).isEqualTo("solar-spain-v1");
                assertThat(response.technical().annualGenerationKwh()).isGreaterThan(400000);

                ArgumentCaptor<Simulation> captor = ArgumentCaptor.forClass(Simulation.class);
                verify(repository, times(2)).save(captor.capture());
                assertThat(captor.getAllValues().get(1).getResultSnapshot()).isNotBlank();
                assertThat(captor.getAllValues().get(1).getTechnologyIds()).containsExactly(1L, 2L);
                verify(technologyLookupPort).recommendActiveTechnologyIdsByEnergyType("solar");
        }

        @Test
        @DisplayName("createSimulation rejects not-yet-implemented wind before persisting a draft")
        void createSimulationRejectsNotImplementedWindBeforePersisting() {
                CreateSimulationService service = new CreateSimulationService(
                                repository,
                                technologyLookupPort,
                                engines(),
                                new SimulationCompletionMapper(new ObjectMapper().findAndRegisterModules()));

                CreateRealSimulationCommand command = new CreateRealSimulationCommand(
                                validCommand().name(),
                                Technology.of("wind"),
                                validCommand().location(),
                                validCommand().system(),
                                validCommand().demand(),
                                validCommand().economics(),
                                validCommand().technologyIds(),
                                validCommand().scenarioId(),
                                validCommand().createdBy());

                when(technologyLookupPort.existsActiveByEnergyType("wind")).thenReturn(true);

                assertThatThrownBy(() -> service.createSimulation(command))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("UNSUPPORTED_TECHNOLOGY: 'wind' simulation is not implemented yet");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("createSimulation rejects caller supplied technology ids that are inactive")
        void createSimulationRejectsCallerSuppliedTechnologyIdsThatAreInactive() {
                CreateSimulationService service = new CreateSimulationService(
                                repository,
                                technologyLookupPort,
                                engines(),
                                new SimulationCompletionMapper(new ObjectMapper().findAndRegisterModules()));

                CreateRealSimulationCommand command = new CreateRealSimulationCommand(
                                validCommand().name(),
                                validCommand().technology(),
                                validCommand().location(),
                                validCommand().system(),
                                validCommand().demand(),
                                validCommand().economics(),
                                List.of(99L),
                                null,
                                validCommand().createdBy());

                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(99L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.createSimulation(command))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("UNSUPPORTED_TECHNOLOGY_ID: '99' is not registered or is inactive in the technology catalog");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("createSimulation rejects caller supplied technology ids that do not match the simulation energy type")
        void createSimulationRejectsCallerSuppliedTechnologyIdsThatDoNotMatchTheSimulationEnergyType() {
                CreateSimulationService service = new CreateSimulationService(
                                repository,
                                technologyLookupPort,
                                engines(),
                                new SimulationCompletionMapper(new ObjectMapper().findAndRegisterModules()));

                CreateRealSimulationCommand command = new CreateRealSimulationCommand(
                                validCommand().name(),
                                validCommand().technology(),
                                validCommand().location(),
                                validCommand().system(),
                                validCommand().demand(),
                                validCommand().economics(),
                                List.of(15L),
                                null,
                                validCommand().createdBy());

                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);
                when(technologyLookupPort.findActiveEnergyTypeByTechnologyId(15L)).thenReturn(Optional.of("wind"));

                assertThatThrownBy(() -> service.createSimulation(command))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("INCOMPATIBLE_TECHNOLOGY_ID: '15' does not belong to energyType 'solar'");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("createSimulation rejects caller supplied technology ids that are duplicated")
        void createSimulationRejectsCallerSuppliedTechnologyIdsThatAreDuplicated() {
                CreateSimulationService service = new CreateSimulationService(
                                repository,
                                technologyLookupPort,
                                engines(),
                                new SimulationCompletionMapper(new ObjectMapper().findAndRegisterModules()));

                CreateRealSimulationCommand command = new CreateRealSimulationCommand(
                                validCommand().name(),
                                validCommand().technology(),
                                validCommand().location(),
                                validCommand().system(),
                                validCommand().demand(),
                                validCommand().economics(),
                                List.of(11L, 11L),
                                null,
                                validCommand().createdBy());

                when(technologyLookupPort.existsActiveByEnergyType("solar")).thenReturn(true);

                assertThatThrownBy(() -> service.createSimulation(command))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessage("DUPLICATE_TECHNOLOGY_IDS: technologyIds must not contain duplicates");

                verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("getSimulationById enforces ownership for non admins")
        void getSimulationByIdEnforcesOwnership() throws Exception {
                GetSimulationService service = new GetSimulationService(repository,
                                new ObjectMapper().findAndRegisterModules());
                SimulationDetailsResult result = minimalResult();
                Simulation simulation = completedSimulation(55L, "alice",
                                new ObjectMapper().writeValueAsString(result));
                when(repository.findById(55L)).thenReturn(Optional.of(simulation));

                assertThatThrownBy(() -> service.getSimulationById(55L, "bob", false))
                                .isInstanceOf(AccessDeniedException.class);
                assertThat(service.getSimulationById(55L, "alice", false).id()).isEqualTo("55");
        }

        @Test
        @DisplayName("getUserSimulations maps stored summary columns for scenario-created simulations")
        void getUserSimulationsMapsStoredSummaryColumnsForScenarioCreatedSimulations() {
                ListSimulationsService service = new ListSimulationsService(repository);
                Simulation simulation = completedSimulation(55L, "alice", null);
                simulation.assignScenarioId(42L);
                simulation.assignTechnologyIds(List.of(11L, 12L));
                when(repository.findByCreatedByOrderByCreatedAtDesc("alice")).thenReturn(List.of(simulation));

                UserSimulationListResult response = service.getUserSimulations("alice");

                assertThat(response.total()).isEqualTo(1);
                assertThat(response.items().getFirst().annualSavings()).isEqualTo(68700.0);
                assertThat(response.items().getFirst().technology()).isEqualTo("solar");
        }

        @Test
        @DisplayName("getDashboard aggregates portfolio KPIs, recommendation and alerts")
        void getDashboardAggregatesPortfolioData() throws Exception {
                GetPortfolioDashboardService service = new GetPortfolioDashboardService(
                                repository,
                                technologyLookupPort,
                                new ObjectMapper().findAndRegisterModules());

                Simulation best = completedSimulation(55L, "alice",
                                new ObjectMapper().writeValueAsString(resultWithMetrics(
                                                "55", "recommended", 82000, 9100, 6.2, 1820, List.of())));
                best.assignScenarioId(42L);
                best.assignTechnologyIds(List.of(11L, 12L));
                Simulation risky = completedSimulation(56L, "alice",
                                new ObjectMapper().writeValueAsString(resultWithMetrics(
                                                "56", "not_recommended", 12000, -5000, 12.4, 980,
                                                List.of(new SimulationDetailsResult.SimulationWarning("warning",
                                                                "LOW_AVAILABILITY_ASSUMPTION",
                                                                "Availability assumption is below 95% and may materially reduce annual output.")))));
                risky.assignScenarioId(43L);
                risky.assignTechnologyIds(List.of(12L, 13L));
                Simulation draft = Simulation.create(
                                "Solar - Draft",
                                Technology.solar(),
                                SimulationLocation.of("Cordoba, Andalucia, ES", 37.8882, -4.7794, "Spain",
                                                CountryCode.of("ES")),
                                new SimulationSystem(120, 0.81, 0.5, 99.0,
                                                new SimulationSystem.LossesPct(2, 6, 1, 3, 1)),
                                ConsumptionProfile.of(48000,
                                                List.of(4000d, 4000d, 4000d, 4000d, 4000d, 4000d, 4000d, 4000d, 4000d,
                                                                4000d, 4000d, 4000d)),
                                new SimulationEconomics(Currency.of("EUR"), 110000.0, 3000.0, 0.18, 0.07, 8,
                                                ProjectLifetime.of(20)),
                                List.of(),
                                null,
                                "alice");
                draft.assignId(SimulationId.of(57L));
                draft.assignScenarioId(44L);
                draft.assignTechnologyIds(List.of(11L));

                when(repository.findByCreatedByOrderByCreatedAtDesc("alice")).thenReturn(List.of(best, risky, draft));
                when(technologyLookupPort.findActiveCo2ReductionFactorByEnergyType("solar"))
                                .thenReturn(Optional.of(0.45));

                PortfolioDashboardResult response = service.getDashboard("alice");

                assertThat(response.summary().totalSimulations()).isEqualTo(3);
                assertThat(response.summary().atRiskCount()).isEqualTo(2);
                assertThat(response.recommendedScenario()).isNotNull();
                assertThat(response.recommendedScenario().id()).isEqualTo("55");
                assertThat(response.prioritizedScenarios()).hasSize(3);
                assertThat(response.riskAlerts()).extracting(PortfolioDashboardRiskAlert::type)
                                .contains("NEGATIVE_ROI", "LONG_PAYBACK", "INCOMPLETE_DATA", "REQUIRES_REVIEW");
                assertThat(response.distribution().byTechnology().getFirst().label()).isEqualTo("SOLAR");
        }

        @Test
        @DisplayName("getDashboard tolerates partial historical snapshots")
        void getDashboardToleratesPartialHistoricalSnapshots() throws Exception {
                GetPortfolioDashboardService service = new GetPortfolioDashboardService(
                                repository,
                                technologyLookupPort,
                                new ObjectMapper().findAndRegisterModules());

                Simulation partial = completedSimulation(58L, "alice", """
                                {
                                  "id": "58",
                                  "status": "completed",
                                  "summary": null,
                                  "technical": null,
                                  "financial": null,
                                  "warnings": null
                                }
                                """);

                when(repository.findByCreatedByOrderByCreatedAtDesc("alice")).thenReturn(List.of(partial));

                PortfolioDashboardResult response = service.getDashboard("alice");

                assertThat(response.summary().totalSimulations()).isEqualTo(1);
                assertThat(response.summary().atRiskCount()).isEqualTo(1);
                assertThat(response.prioritizedScenarios()).hasSize(1);
                assertThat(response.prioritizedScenarios().getFirst().priority()).isEqualTo("REVIEW");
        }

        @Test
        @DisplayName("ConsumptionProfile rejects annual demand mismatches at domain level")
        void consumptionProfileRejectsAnnualDemandMismatch() {
                assertThatThrownBy(() -> ConsumptionProfile.of(999999,
                                List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                                10000d, 10000d)))
                                .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("getSimulationById fails when snapshot is missing")
        void getSimulationByIdFailsWhenSnapshotMissing() {
                GetSimulationService service = new GetSimulationService(repository,
                                new ObjectMapper().findAndRegisterModules());
                Simulation simulation = completedSimulation(55L, "alice", null);
                when(repository.findById(55L)).thenReturn(Optional.of(simulation));

                assertThatThrownBy(() -> service.getSimulationById(55L, "alice", false))
                                .isInstanceOf(SimulationNotFoundException.class);
        }

        private CreateRealSimulationCommand validCommand() {
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

        private List<SimulationEngine> engines() {
                return List.of(
                                new SolarSimulationEngine(resourcePort, new SolarSimulationAssessmentPolicy()),
                                new WindSimulationEngine(),
                                new HydroSimulationEngine());
        }

        private Simulation completedSimulation(Long id, String owner, String resultJson) {
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

        private PvgisSolarResourcePort.PvgisSolarResourceProfile profile() {
                return new PvgisSolarResourcePort.PvgisSolarResourceProfile(
                                List.of(117.91, 117.78, 140.16, 142.40, 155.64, 154.71, 164.37, 161.96, 145.57, 132.00,
                                                112.14, 112.69),
                                List.of(144.21, 146.57, 177.98, 185.86, 208.58, 212.34, 230.04, 226.42, 197.02, 172.86,
                                                140.05, 137.50),
                                List.of(10.0, 12.0, 15.0, 17.0, 22.0, 27.0, 31.0, 31.0, 27.0, 21.0, 15.0, 11.0),
                                "2005-2020",
                                "PVGIS");
        }

        private SimulationDetailsResult minimalResult() {
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

        private SimulationDetailsResult resultWithMetrics(
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
}
