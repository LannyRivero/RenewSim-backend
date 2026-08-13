package com.renewsim.backend.simulation_service.dashboard.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.dashboard.application.port.out.PortfolioDashboardQueryPort;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardRiskAlert;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardResult;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationId;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.renewsim.backend.simulation_service.support.SimulationDashboardTestFixtures.dashboardAggregator;
import static com.renewsim.backend.simulation_service.support.SimulationDashboardTestFixtures.resultWithMetrics;
import static com.renewsim.backend.simulation_service.support.SimulationDashboardTestFixtures.snapshotAssembler;
import static com.renewsim.backend.simulation_service.support.SimulationDetailTestFixtures.completedSimulation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPortfolioDashboardServiceTest {

        @Mock
        private PortfolioDashboardQueryPort repository;
        @Mock
        private TechnologyLookupPort technologyLookupPort;

        private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        @Test
        @DisplayName("getDashboard aggregates portfolio KPIs, recommendation and alerts")
        void getDashboardAggregatesPortfolioData() throws Exception {
                GetPortfolioDashboardService service = new GetPortfolioDashboardService(
                                repository,
                                snapshotAssembler(technologyLookupPort),
                                dashboardAggregator(),
                                new SimulationUseCaseTelemetry(meterRegistry));

                Simulation best = completedSimulation(55L, "alice",
                                new ObjectMapper().findAndRegisterModules().writeValueAsString(resultWithMetrics(
                                                "55", "recommended", 82000, 9100, 6.2, 1820, List.of())));
                best.assignScenarioId(42L);
                best.assignTechnologyIds(List.of(11L, 12L));
                Simulation risky = completedSimulation(56L, "alice",
                                new ObjectMapper().findAndRegisterModules().writeValueAsString(resultWithMetrics(
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
                                snapshotAssembler(technologyLookupPort),
                                dashboardAggregator(),
                                new SimulationUseCaseTelemetry(meterRegistry));

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
        @DisplayName("getDashboard tolerates malformed historical snapshots")
        void getDashboardToleratesMalformedHistoricalSnapshots() {
                GetPortfolioDashboardService service = new GetPortfolioDashboardService(
                                repository,
                                snapshotAssembler(technologyLookupPort),
                                dashboardAggregator(),
                                new SimulationUseCaseTelemetry(meterRegistry));

                Simulation malformed = completedSimulation(59L, "alice", "{ bad json");

                when(repository.findByCreatedByOrderByCreatedAtDesc("alice")).thenReturn(List.of(malformed));

                PortfolioDashboardResult response = service.getDashboard("alice");

                assertThat(response.summary().totalSimulations()).isEqualTo(1);
                assertThat(response.summary().atRiskCount()).isEqualTo(1);
                assertThat(response.prioritizedScenarios()).hasSize(1);
                assertThat(response.prioritizedScenarios().getFirst().priority()).isEqualTo("REVIEW");
        }
}
