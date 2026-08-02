package com.renewsim.backend.simulation_service.dashboard.web;

import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardDistribution;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardDistributionByStatus;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardDistributionByTechnology;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardPrioritizedScenario;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardRecommendedScenario;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardResult;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardRiskAlert;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardSummary;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationDashboardWebMapperTest {

    private final SimulationDashboardWebMapper mapper = new SimulationDashboardWebMapper();

    @Test
    @DisplayName("toWebDashboard preserves dashboard projection")
    void toWebDashboardPreservesDashboardProjection() {
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

        assertThat(dashboardResponse.summary().totalSimulations()).isEqualTo(3);
        assertThat(dashboardResponse.recommendedScenario().name()).isEqualTo("Solar - Sevilla");
        assertThat(dashboardResponse.distribution().byTechnology().getFirst().energyKwh()).isEqualTo(912300.0);
    }
}
