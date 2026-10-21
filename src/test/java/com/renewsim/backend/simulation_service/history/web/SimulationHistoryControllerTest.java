package com.renewsim.backend.simulation_service.history.web;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.shared.security.AuthenticatedRequestContext;
import com.renewsim.backend.simulation_service.detail.application.port.in.GetRealSimulationUseCase;
import com.renewsim.backend.simulation_service.history.application.port.in.ListUserRealSimulationsUseCase;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPageRequest;
import com.renewsim.backend.simulation_service.history.application.result.SimulationHistoryRowResult;
import com.renewsim.backend.simulation_service.history.application.result.UserSimulationListResult;
import com.renewsim.backend.simulation_service.detail.web.SimulationDetailController;
import com.renewsim.backend.shared.security.AuthenticatedRequestContextFactory;
import com.renewsim.backend.simulation_service.history.web.dto.ListUserSimulationsResponseDTO;
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
    private GetRealSimulationUseCase getRealSimulationUseCase;
    @Mock
    private AuthenticatedRequestContextFactory requestContextFactory;

    @InjectMocks
    private SimulationHistoryController controller;

    @Test
    @DisplayName("getMySimulations returns list projection contract")
    void getMySimulationsReturnsListProjectionContract() {
        Authentication auth = authentication("alice", "ROLE_USER");
        when(requestContextFactory.from(auth)).thenReturn(new AuthenticatedRequestContext("alice", false));
        when(listUserRealSimulationsUseCase.getUserSimulations(org.mockito.ArgumentMatchers.eq("alice"),
                org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any(SimulationHistoryPageRequest.class))).thenReturn(
                new UserSimulationListResult(
                        List.of(new SimulationHistoryRowResult("55", "Solar - Sevilla", "solar", "completed",
                                "2026-06-30T14:00:00Z", "Sevilla, Andalucia, ES", 457200, 68700, 121500, 11.4,
                                "viable_with_reservations", "solar-spain-v1", "PVGIS")),
                        1, 0, 20, 1));

        ListUserSimulationsResponseDTO body = controller.getMySimulations(auth, null, 0, 20, "createdAt", "desc").getBody();

        assertThat(body).isNotNull();
        assertThat(body.total()).isEqualTo(1);
        assertThat(body.items().getFirst().recommendation()).isEqualTo("viable_with_reservations");
    }

    @Test
    @DisplayName("get user simulations route does not collide with numeric id mapping")
    void getUserSimulationsRouteDoesNotCollideWithIdRoute() throws Exception {
        Authentication auth = authentication("alice", "ROLE_USER");
        when(requestContextFactory.from(auth)).thenReturn(new AuthenticatedRequestContext("alice", false));
        SimulationDetailController detailController = new SimulationDetailController(getRealSimulationUseCase,
                new AuthenticatedRequestContextFactory());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller, detailController).build();

        when(listUserRealSimulationsUseCase.getUserSimulations(org.mockito.ArgumentMatchers.eq("alice"),
                org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any(SimulationHistoryPageRequest.class)))
                .thenReturn(new UserSimulationListResult(List.of(), 0, 0, 20, 0));

        mockMvc.perform(get("/api/v1/simulations/user").principal(auth).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(listUserRealSimulationsUseCase).getUserSimulations(org.mockito.ArgumentMatchers.eq("alice"),
                org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any(SimulationHistoryPageRequest.class));
        verify(getRealSimulationUseCase, never()).getSimulationById(any(), any(), any(Boolean.class));
    }

    @Test
    @DisplayName("history endpoint applies location, pagination and ascending sort")
    void historyEndpointAppliesQueryParameters() throws Exception {
        Authentication auth = authentication("alice", "ROLE_USER");
        when(requestContextFactory.from(auth)).thenReturn(new AuthenticatedRequestContext("alice", false));
        when(listUserRealSimulationsUseCase.getUserSimulations(org.mockito.ArgumentMatchers.eq("alice"),
                org.mockito.ArgumentMatchers.eq("Málaga"), org.mockito.ArgumentMatchers.any(SimulationHistoryPageRequest.class)))
                .thenReturn(new UserSimulationListResult(List.of(), 0, 2, 5, 0));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/simulations/user")
                        .principal(auth)
                        .param("location", "Málaga")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortBy", "technology")
                        .param("direction", "asc"))
                .andExpect(status().isOk());

        var pageRequestCaptor = org.mockito.ArgumentCaptor.forClass(SimulationHistoryPageRequest.class);
        verify(listUserRealSimulationsUseCase).getUserSimulations(
                org.mockito.ArgumentMatchers.eq("alice"), org.mockito.ArgumentMatchers.eq("Málaga"), pageRequestCaptor.capture());
        assertThat(pageRequestCaptor.getValue().page()).isEqualTo(2);
        assertThat(pageRequestCaptor.getValue().size()).isEqualTo(5);
        assertThat(pageRequestCaptor.getValue().sortField()).isEqualTo(SimulationHistoryPageRequest.SortField.TECHNOLOGY);
        assertThat(pageRequestCaptor.getValue().sortDirection()).isEqualTo(SimulationHistoryPageRequest.SortDirection.ASC);
    }

    @Test
    @DisplayName("saved location suggestions are scoped to the authenticated user and bounded")
    void savedLocationSuggestionsAreScopedToAuthenticatedUserAndBounded() {
        Authentication auth = authentication("alice", "ROLE_USER");
        when(requestContextFactory.from(auth)).thenReturn(new AuthenticatedRequestContext("alice", false));
        when(listUserRealSimulationsUseCase.getUserLocationSuggestions("alice", "Sevilla", 10))
                .thenReturn(List.of("Sevilla, ES"));

        var response = controller.getUserLocationSuggestions(auth, " Sevilla ", 50);

        assertThat(response.getBody()).containsExactly("Sevilla, ES");
        verify(listUserRealSimulationsUseCase).getUserLocationSuggestions("alice", "Sevilla", 10);
    }

    @Test
    @DisplayName("saved location suggestions return empty for queries shorter than two characters")
    void savedLocationSuggestionsReturnEmptyForShortQueries() {
        Authentication auth = authentication("alice", "ROLE_USER");

        var response = controller.getUserLocationSuggestions(auth, " S ", 5);

        assertThat(response.getBody()).isEmpty();
        verify(listUserRealSimulationsUseCase, never()).getUserLocationSuggestions(any(), any(), any(int.class));
    }

    private Authentication authentication(String username, String role) {
        AuthenticatedUser user = AuthenticatedUser.of(username, Set.of(role.replace("ROLE_", "")),
                Set.of("read:simulations", "write:simulations"));
        return new TestingAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority(role)));
    }
}
