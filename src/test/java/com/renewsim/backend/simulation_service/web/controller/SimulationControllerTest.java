package com.renewsim.backend.simulation_service.web.controller;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.simulation_service.application.createSimulation.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.application.dashboard.GetPortfolioDashboardUseCase;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardResult;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardDistribution;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardDistributionByStatus;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardDistributionByTechnology;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardPrioritizedScenario;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardRecommendedScenario;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardRiskAlert;
import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardSummary;
import com.renewsim.backend.simulation_service.application.deleteSimulation.DeleteRealSimulationUseCase;
import com.renewsim.backend.simulation_service.application.detailSimulation.GetRealSimulationUseCase;
import com.renewsim.backend.simulation_service.application.historySimulation.ListUserRealSimulationsUseCase;
import com.renewsim.backend.simulation_service.application.historySimulation.SimulationHistoryRowResult;
import com.renewsim.backend.simulation_service.application.historySimulation.UserSimulationListResult;
import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.web.dto.CreateSolarSimulationRequestDTO;
import com.renewsim.backend.simulation_service.web.dto.ListUserSimulationsResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.PortfolioDashboardResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationDetailsResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SimulationControllerTest {

    @Mock
    private CreateRealSimulationUseCase createRealSimulationUseCase;
    @Mock
    private GetRealSimulationUseCase getRealSimulationUseCase;
    @Mock
    private GetPortfolioDashboardUseCase getPortfolioDashboardUseCase;
    @Mock
    private ListUserRealSimulationsUseCase listUserRealSimulationsUseCase;
    @Mock
    private DeleteRealSimulationUseCase deleteRealSimulationUseCase;

    @InjectMocks
    private SimulationController controller;

    @Test
    @DisplayName("createSimulation returns the real simulation details contract")
    void createSimulationReturnsRealContract() {
        Authentication auth = authentication("alice", "ROLE_USER");
        CreateSolarSimulationRequestDTO request = request();

        when(createRealSimulationUseCase.createSimulation(any())).thenReturn(sampleResult());

        SimulationDetailsResponseDTO body = controller.createSimulation(request, auth).getBody();

        assertThat(body).isNotNull();
        assertThat(body.id()).isEqualTo("55");
        assertThat(body.technical().annualGenerationKwh()).isEqualTo(457200);
        verify(createRealSimulationUseCase).createSimulation(any());
    }

    @Test
    @DisplayName("getSimulationById returns the real details contract")
    void getSimulationByIdReturnsRealDetailsContract() {
        Authentication auth = authentication("alice", "ROLE_USER");
        when(getRealSimulationUseCase.getSimulationById(55L, "alice", false)).thenReturn(sampleResult());

        SimulationDetailsResponseDTO body = controller.getSimulationById(55L, auth).getBody();

        assertThat(body).isNotNull();
        assertThat(body.summary().recommendation()).isEqualTo("viable_with_reservations");
        assertThat(body.technical().resource().source()).isEqualTo("PVGIS");
    }

    @Test
    @DisplayName("getMySimulations returns list projection contract")
    void getMySimulationsReturnsListProjectionContract() {
        Authentication auth = authentication("alice", "ROLE_USER");
        when(listUserRealSimulationsUseCase.getUserSimulations("alice")).thenReturn(
                new UserSimulationListResult(
                        List.of(new SimulationHistoryRowResult("55", "Solar - Sevilla", "solar", "completed",
                                "2026-06-30T14:00:00Z", "Sevilla, Andalucia, ES", 457200, 68700, 121500, 11.4,
                                "viable_with_reservations", "solar-spain-v1", "PVGIS")),
                        1));

        ListUserSimulationsResponseDTO body = controller.getMySimulations(auth).getBody();

        assertThat(body).isNotNull();
        assertThat(body.total()).isEqualTo(1);
        assertThat(body.items().getFirst().recommendation()).isEqualTo("viable_with_reservations");
    }

    @Test
    @DisplayName("getDashboard returns executive portfolio contract")
    void getDashboardReturnsExecutivePortfolioContract() {
        Authentication auth = authentication("alice", "ROLE_USER");
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

    @Test
    @DisplayName("get user simulations route does not collide with numeric id mapping")
    void getUserSimulationsRouteDoesNotCollideWithIdRoute() throws Exception {
        Authentication auth = authentication("alice", "ROLE_USER");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(listUserRealSimulationsUseCase.getUserSimulations("alice"))
                .thenReturn(new UserSimulationListResult(List.of(), 0));

        mockMvc.perform(get("/api/v1/simulations/user").principal(auth).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(listUserRealSimulationsUseCase).getUserSimulations("alice");
        verify(getRealSimulationUseCase, never()).getSimulationById(any(), any(), any(Boolean.class));
    }

    private CreateSolarSimulationRequestDTO request() {
        return new CreateSolarSimulationRequestDTO(
                "Solar - Sevilla",
                new CreateSolarSimulationRequestDTO.LocationDTO("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", "ES"),
                new CreateSolarSimulationRequestDTO.SolarSystemDTO(300, 0.81, 0.5, 99,
                        new CreateSolarSimulationRequestDTO.LossesPctDTO(2, 6, 1, 3, 1)),
                new CreateSolarSimulationRequestDTO.DemandDTO(120000,
                        List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                10000d)),
                new CreateSolarSimulationRequestDTO.EconomicsDTO("EUR", 315000, 7200, 0.18, 0.07, 8, 20));
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

    private Authentication authentication(String username, String role) {
        AuthenticatedUser user = AuthenticatedUser.of(username, Set.of(role.replace("ROLE_", "")),
                Set.of("read:simulations", "write:simulations"));
        return new TestingAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority(role)));
    }
}
