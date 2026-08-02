package com.renewsim.backend.simulation_service.web.controller;

import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardDistribution;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardDistributionByStatus;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardDistributionByTechnology;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardPrioritizedScenario;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardRecommendedScenario;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardResult;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardRiskAlert;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardSummary;
import com.renewsim.backend.simulation_service.application.historySimulation.SimulationHistoryRowResult;
import com.renewsim.backend.simulation_service.application.historySimulation.UserSimulationListResult;
import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.web.dto.ListUserSimulationsResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.PortfolioDashboardResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationDetailsResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationResponseWebMapperTest {

    private final SimulationWebMapper mapper = new SimulationWebMapper();

    @Test
    @DisplayName("toWebDetails maps nested simulation details contract")
    void toWebDetailsMapsNestedSimulationDetailsContract() {
        SimulationDetailsResponseDTO response = mapper.toWebDetails(sampleResult());

        assertThat(response.id()).isEqualTo("55");
        assertThat(response.summary().recommendation()).isEqualTo("viable_with_reservations");
        assertThat(response.input().economics().currency()).isEqualTo("EUR");
        assertThat(response.technical().resource().source()).isEqualTo("PVGIS");
        assertThat(response.financial().yearlyCashFlows()).hasSize(1);
        assertThat(response.warnings()).hasSize(1);
    }

    @Test
    @DisplayName("toWebList and toWebDashboard preserve response projections")
    void toWebListAndToWebDashboardPreserveResponseProjections() {
        ListUserSimulationsResponseDTO listResponse = mapper.toWebList(new UserSimulationListResult(
                List.of(new SimulationHistoryRowResult("55", "Solar - Sevilla", "solar", "completed",
                        "2026-06-30T14:00:00Z", "Sevilla", 457200, 68700, 121500, 11.4, "viable_with_reservations",
                        "solar-spain-v1", "PVGIS")),
                1));
        PortfolioDashboardResponseDTO dashboardResponse = mapper.toWebDashboard(new PortfolioDashboardResult(
                new PortfolioDashboardSummary(3, 3, 14.2, 6.2, 912300, 410535, 2),
                new PortfolioDashboardRecommendedScenario("55", "Solar - Sevilla", "SOLAR", "Sevilla, ES", 22.5, 6.2,
                        315000.0, 82000.0, "HIGH", "headline", List.of("driver 1"), "main risk", "next step"),
                List.of(new PortfolioDashboardPrioritizedScenario("55", "Solar - Sevilla", "SOLAR", "COMPLETED",
                        "Sevilla, ES", 22.5, 6.2, 315000.0, 82000.0, "HIGH", 82)),
                List.of(new PortfolioDashboardRiskAlert("INCOMPLETE_DATA", "MEDIUM", 1,
                        "1 simulaciones no tienen información suficiente para priorizar")),
                new PortfolioDashboardDistribution(
                        List.of(new PortfolioDashboardDistributionByTechnology("SOLAR", 3, 912300)),
                        List.of(new PortfolioDashboardDistributionByStatus("COMPLETED", 2)))));

        assertThat(listResponse.total()).isEqualTo(1);
        assertThat(listResponse.items().getFirst().technology()).isEqualTo("solar");
        assertThat(dashboardResponse.summary().totalSimulations()).isEqualTo(3);
        assertThat(dashboardResponse.recommendedScenario().name()).isEqualTo("Solar - Sevilla");
        assertThat(dashboardResponse.distribution().byTechnology().getFirst().energyKwh()).isEqualTo(912300.0);
    }

    private SimulationDetailsResult sampleResult() {
        return new SimulationDetailsResult(
                "55",
                "completed",
                "2026-06-30T14:00:00Z",
                "2026-06-30T14:00:00Z",
                "solar-spain-v1",
                "solar",
                new SimulationDetailsResult.ResolvedLocation("Sevilla, Andalucia, ES", "Sevilla", "Andalucia", "Spain",
                        "ES", 37.3891, -5.9845, "Europe/Madrid"),
                new SimulationDetailsResult.Summary("viable_with_reservations", "headline", "summary",
                        List.of(new SimulationDetailsResult.RecommendationReason("resource", "positive", "msg"))),
                new SimulationDetailsResult.Input(
                        "Solar - Sevilla",
                        "solar",
                        new SimulationDetailsResult.Location("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", "ES"),
                        new SimulationDetailsResult.SystemSpec(300, 0.81, 0.5, 99,
                                new SimulationDetailsResult.LossesPct(2, 6, 1, 3, 1)),
                        new SimulationDetailsResult.Demand(120000,
                                List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                        10000d, 10000d)),
                        new SimulationDetailsResult.Economics("EUR", 315000, 7200, 0.18, 0.07, 8, 20)),
                new SimulationDetailsResult.Technical(457200, List.of(24800d, 29100d), 1524, 0.81, 17.4, 72.3, 31.5,
                        new SimulationDetailsResult.ResourceSeries("PVGIS", "2005-2020", List.of(71d), List.of(10d)),
                        new SimulationDetailsResult.LossesSummary(2, 6, 1, 3, 1, 13),
                        List.of(new SimulationDetailsResult.MonthlyEnergyBalanceItem("Jan", 24800, 10000, 10000, 14800,
                                0))),
                new SimulationDetailsResult.Financial("EUR", 68700, 8800, 70300, 6.9, 8.7, 121500, 11.4, 0.071,
                        List.of(new SimulationDetailsResult.FinancialYearItem(0, 0, 0, 0, 0, -315000, -315000,
                                -315000))),
                new SimulationDetailsResult.Assumptions(8, 20, 0.5, 0.18, 0.07, "PVGIS", "2005-2020"),
                List.of(new SimulationDetailsResult.SimulationWarning("info", "MONTHLY_PROFILE_USER_SUPPLIED",
                        "warning")));
    }
}
