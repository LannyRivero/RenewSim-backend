package com.renewsim.backend.simulation_service.dashboard.web;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.shared.security.AuthenticatedRequestContext;
import com.renewsim.backend.shared.security.AuthenticatedRequestContextFactory;
import com.renewsim.backend.simulation_service.dashboard.application.port.in.GetPortfolioDashboardUseCase;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardDistribution;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardDistributionByStatus;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardDistributionByTechnology;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardPrioritizedScenario;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardRecommendedScenario;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardRiskAlert;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardSummary;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardResult;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationDashboardControllerTest {

    @Mock
    private GetPortfolioDashboardUseCase getPortfolioDashboardUseCase;
    @Mock
    private AuthenticatedRequestContextFactory requestContextFactory;

    @InjectMocks
    private SimulationDashboardController controller;

    @Test
    @DisplayName("getDashboard returns executive portfolio contract")
    void getDashboardReturnsExecutivePortfolioContract() {
        Authentication auth = authentication("alice", "ROLE_USER");
        when(requestContextFactory.from(auth)).thenReturn(new AuthenticatedRequestContext("alice", false));
        when(getPortfolioDashboardUseCase.getDashboard("alice")).thenReturn(
                new PortfolioDashboardResult(
                        new PortfolioDashboardSummary(3, 3, 14.2, 6.2, 912300, 410535, 2),
                        new PortfolioDashboardRecommendedScenario(
                                "55", "Solar - Sevilla", "SOLAR", "Sevilla, ES", 22.5, 6.2, 315000.0, 82000.0,
                                "HIGH", "headline", List.of("driver 1"), "main risk", "next step"),
                        List.of(new PortfolioDashboardPrioritizedScenario(
                                "55", "Solar - Sevilla", "SOLAR", "COMPLETED", "Sevilla, ES", 22.5, 6.2,
                                315000.0, 82000.0, "HIGH", 82)),
                        List.of(new PortfolioDashboardRiskAlert("INCOMPLETE_DATA", "MEDIUM", 1,
                                "1 simulaciones no tienen información suficiente para priorizar")),
                        new PortfolioDashboardDistribution(
                                List.of(new PortfolioDashboardDistributionByTechnology("SOLAR", 3, 912300)),
                                List.of(new PortfolioDashboardDistributionByStatus("COMPLETED", 2),
                                        new PortfolioDashboardDistributionByStatus("DRAFT", 1)))));

        PortfolioDashboardResponseDTO body = controller.getDashboard(auth).getBody();

        assertThat(body).isNotNull();
        assertThat(body.summary().totalSimulations()).isEqualTo(3);
        assertThat(body.recommendedScenario()).isNotNull();
        assertThat(body.recommendedScenario().name()).isEqualTo("Solar - Sevilla");
        assertThat(body.riskAlerts().getFirst().type()).isEqualTo("INCOMPLETE_DATA");
    }

    private Authentication authentication(String username, String role) {
        AuthenticatedUser user = AuthenticatedUser.of(username, Set.of(role.replace("ROLE_", "")), Set.of("read:simulations", "write:simulations"));
        return new TestingAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority(role)));
    }
}
