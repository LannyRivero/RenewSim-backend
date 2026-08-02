package com.renewsim.backend.simulation_service.create.web;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.create.web.dto.CreateSolarSimulationRequestDTO;
import com.renewsim.backend.simulation_service.shared.web.dto.SimulationDetailsResponseDTO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSimulationControllerTest {

    @Mock
    private CreateRealSimulationUseCase createRealSimulationUseCase;

    @InjectMocks
    private CreateSimulationController controller;

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
                "55", "completed", "2026-06-30T14:00:00Z", "2026-06-30T14:00:00Z", "solar-spain-v1", "solar",
                new SimulationDetailsResult.ResolvedLocation("Sevilla, Andalucia, ES", "Sevilla", "Andalucia", "Spain", "ES", 37.3891, -5.9845, "Europe/Madrid"),
                new SimulationDetailsResult.Summary("viable_with_reservations", "headline", "summary", List.of(new SimulationDetailsResult.RecommendationReason("resource", "positive", "msg"))),
                new SimulationDetailsResult.Input("Solar - Sevilla", "solar", new SimulationDetailsResult.Location("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", "ES"), new SimulationDetailsResult.SystemSpec(300, 0.81, 0.5, 99, new SimulationDetailsResult.LossesPct(2, 6, 1, 3, 1)), new SimulationDetailsResult.Demand(120000, List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d)), new SimulationDetailsResult.Economics("EUR", 315000, 7200, 0.18, 0.07, 8, 20)),
                new SimulationDetailsResult.Technical(457200, List.of(24800d, 29100d), 1524, 0.81, 17.4, 72.3, 31.5, new SimulationDetailsResult.ResourceSeries("PVGIS", "2005-2020", List.of(71d), List.of(10d)), new SimulationDetailsResult.LossesSummary(2, 6, 1, 3, 1, 13), List.of(new SimulationDetailsResult.MonthlyEnergyBalanceItem("Jan", 24800, 10000, 10000, 14800, 0))),
                new SimulationDetailsResult.Financial("EUR", 68700, 8800, 70300, 6.9, 8.7, 121500, 11.4, 0.071, List.of(new SimulationDetailsResult.FinancialYearItem(0, 0, 0, 0, 0, -315000, -315000, -315000))),
                new SimulationDetailsResult.Assumptions(8, 20, 0.5, 0.18, 0.07, "PVGIS", "2005-2020"),
                List.of(new SimulationDetailsResult.SimulationWarning("info", "MONTHLY_PROFILE_USER_SUPPLIED", "warning")));
    }

    private Authentication authentication(String username, String role) {
        AuthenticatedUser user = AuthenticatedUser.of(username, Set.of(role.replace("ROLE_", "")), Set.of("read:simulations", "write:simulations"));
        return new TestingAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority(role)));
    }
}
