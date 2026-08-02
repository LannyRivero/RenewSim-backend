package com.renewsim.backend.simulation_service.history.web;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.simulation_service.application.deleteSimulation.DeleteRealSimulationUseCase;
import com.renewsim.backend.simulation_service.application.detailSimulation.GetRealSimulationUseCase;
import com.renewsim.backend.simulation_service.application.historySimulation.ListUserRealSimulationsUseCase;
import com.renewsim.backend.simulation_service.application.historySimulation.SimulationHistoryRowResult;
import com.renewsim.backend.simulation_service.application.historySimulation.UserSimulationListResult;
import com.renewsim.backend.simulation_service.web.dto.ListUserSimulationsResponseDTO;
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
class SimulationHistoryControllerTest {

    @Mock
    private ListUserRealSimulationsUseCase listUserRealSimulationsUseCase;
    @Mock
    private DeleteRealSimulationUseCase deleteRealSimulationUseCase;
    @Mock
    private GetRealSimulationUseCase getRealSimulationUseCase;

    @InjectMocks
    private SimulationHistoryController controller;

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

    private Authentication authentication(String username, String role) {
        AuthenticatedUser user = AuthenticatedUser.of(username, Set.of(role.replace("ROLE_", "")), Set.of("read:simulations", "write:simulations"));
        return new TestingAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority(role)));
    }
}
