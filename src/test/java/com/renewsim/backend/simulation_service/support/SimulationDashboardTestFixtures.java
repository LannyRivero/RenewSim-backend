package com.renewsim.backend.simulation_service.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.dashboard.application.PortfolioDashboardAggregator;
import com.renewsim.backend.simulation_service.dashboard.application.PortfolioScenarioScoringPolicy;
import com.renewsim.backend.simulation_service.dashboard.application.ScenarioSnapshotAssembler;
import com.renewsim.backend.simulation_service.dashboard.application.ScenarioSnapshotMetricsResolver;
import com.renewsim.backend.simulation_service.dashboard.application.ScenarioSnapshotNarrativeResolver;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.DashboardSnapshotJacksonReader;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;

import java.util.List;

public final class SimulationDashboardTestFixtures {

        private SimulationDashboardTestFixtures() {
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

        public static ScenarioSnapshotAssembler snapshotAssembler(TechnologyLookupPort technologyLookupPort) {
                return new ScenarioSnapshotAssembler(
                                new DashboardSnapshotJacksonReader(new ObjectMapper().findAndRegisterModules()),
                                new PortfolioScenarioScoringPolicy(),
                                new ScenarioSnapshotMetricsResolver(technologyLookupPort),
                                new ScenarioSnapshotNarrativeResolver());
        }

        public static PortfolioDashboardAggregator dashboardAggregator() {
                return new PortfolioDashboardAggregator();
        }
}
