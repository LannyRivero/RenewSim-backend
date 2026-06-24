package com.renewsim.backend.simulation_service.web.controller;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.simulation_service.application.command.GetSimulationByIdCommand;
import com.renewsim.backend.simulation_service.application.createSimulation.CreateSimulationCommand;
import com.renewsim.backend.simulation_service.application.createSimulation.CreateSimulationUseCase;
import com.renewsim.backend.simulation_service.application.createSimulation.SimulationCreationResultDTO;
import com.renewsim.backend.simulation_service.application.deleteSimulation.DeleteAllSimulationsByUserUseCase;
import com.renewsim.backend.simulation_service.application.deleteSimulation.DeleteSimulationUseCase;
import com.renewsim.backend.simulation_service.application.detailSimulation.SimulationDetailResultDTO;
import com.renewsim.backend.simulation_service.application.historySimulation.GetUserSimulationHistoryUseCase;
import com.renewsim.backend.simulation_service.application.historySimulation.SimulationHistoryResultDTO;
import com.renewsim.backend.simulation_service.application.port.in.GetSimulationUseCase;
import com.renewsim.backend.simulation_service.application.port.in.GetSimulationDashboardSummaryUseCase;
import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.application.result.SimulationDashboardSummaryResult;
import com.renewsim.backend.simulation_service.application.result.SimulationDashboardStatsResult;
import com.renewsim.backend.simulation_service.application.result.SimulationDashboardEnergyBySourceResult;
import com.renewsim.backend.simulation_service.application.updateSimulation.UpdateSimulationUseCase;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import com.renewsim.backend.simulation_service.web.dto.CreateSimulationResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.DashboardSummaryResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.ResolvedLocationResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationLocationSummaryDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationLocationRequestDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationRequestDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationResultsResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.UserSimulationSummaryDTO;
import com.renewsim.backend.simulation_service.web.mapper.SimulationResponseMapper;
import com.renewsim.backend.user_service.web.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
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

    @Mock private CreateSimulationUseCase createUseCase;
    @Mock private UpdateSimulationUseCase updateUseCase;
    @Mock private DeleteSimulationUseCase deleteUseCase;
    @Mock private GetSimulationUseCase getUseCase;
    @Mock private GetSimulationDashboardSummaryUseCase dashboardSummaryUseCase;
    @Mock private GetUserSimulationHistoryUseCase historyUseCase;
    @Mock private DeleteAllSimulationsByUserUseCase deleteAllSimulationsByUserUseCase;
    @Mock private ClimateDataProviderPort climateDataProviderPort;
    @Mock private SimulationResponseMapper responseMapper;

    @InjectMocks
    private SimulationController controller;

    @Test
    @DisplayName("createSimulation maps frontend contract into backend-owned command")
    void createSimulationMapsFrontendContract() {
        Authentication auth = authentication("alice", "ROLE_USER");
        SimulationRequestDTO request = new SimulationRequestDTO(
                "Test Solar",
                "solar",
                100,
                new SimulationLocationRequestDTO(-32.8895, -68.8458));

        when(createUseCase.createSimulation(any(CreateSimulationCommand.class))).thenReturn(
                new SimulationCreationResultDTO(55L, "Test Solar", LocalDateTime.parse("2026-06-08T10:15:30")));
        when(responseMapper.toCreateResponse(any())).thenReturn(
                new CreateSimulationResponseDTO(55L, "Test Solar", "completed", LocalDateTime.parse("2026-06-08T10:15:30")));

        CreateSimulationResponseDTO response = controller.createSimulation(request, auth).getBody();

        ArgumentCaptor<CreateSimulationCommand> commandCaptor = ArgumentCaptor.forClass(CreateSimulationCommand.class);
        verify(createUseCase).createSimulation(commandCaptor.capture());
        assertThat(commandCaptor.getValue().name()).isEqualTo("Test Solar");
        assertThat(commandCaptor.getValue().latitude()).isEqualTo(-32.8895);
        assertThat(commandCaptor.getValue().longitude()).isEqualTo(-68.8458);
        assertThat(commandCaptor.getValue().budget()).isZero();
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("completed");
    }

    @Test
    @DisplayName("getSimulationById returns final results contract with financials and climate")
    void getSimulationByIdReturnsFinalResultsContract() {
        Authentication auth = authentication("alice", "ROLE_USER");
        SimulationDetailResultDTO detail = new SimulationDetailResultDTO(
                77L,
                "Wind Demo",
                "Cordoba",
                -31.4167,
                -64.1833,
                "WIND",
                80,
                120000,
                245000,
                35.0,
                new ClimateData(450, 9, 40, 18.5, "ERA5", "recent_10yr", "AR"),
                LocalDateTime.parse("2026-06-08T11:00:00"),
                "alice",
                List.of(2L, 3L));

        when(getUseCase.getSimulationById(any(GetSimulationByIdCommand.class))).thenReturn(detail);
        when(responseMapper.toResultsResponse(detail)).thenReturn(
                new SimulationResultsResponseDTO(
                        77L,
                        "Wind Demo",
                        LocalDateTime.parse("2026-06-08T11:00:00"),
                        new com.renewsim.backend.simulation_service.web.dto.SimulationLocationResponseDTO("Cordoba", "AR", -31.4167, -64.1833),
                        new com.renewsim.backend.simulation_service.web.dto.SimulationClimateDataResponseDTO(450, 9, 40, 18.5, "ERA5", "recent_10yr"),
                        "wind",
                        80,
                        120000,
                        35.0,
                        new com.renewsim.backend.simulation_service.web.dto.SimulationFinancialsResponseDTO(245000, 3600, 29400, 21.5, 5.0, 88250, 13.2),
                        null,
                        null));

        SimulationResultsResponseDTO response = controller.getSimulationById(77L, auth).getBody();

        assertThat(response).isNotNull();
        assertThat(response.location().lat()).isEqualTo(-31.4167);
        assertThat(response.climateData().windSpeed()).isEqualTo(9);
        assertThat(response.financials().roi()).isEqualTo(21.5);
        assertThat(response.technology()).isEqualTo("wind");
    }

    @Test
    @DisplayName("getMySimulations returns paginated completed history")
    void getMySimulationsReturnsPaginatedHistory() {
        Authentication auth = authentication("alice", "ROLE_USER");

        SimulationHistoryResultDTO first = new SimulationHistoryResultDTO(
                10L,
                "Solar One",
                "Mendoza",
                "AR",
                -32.8895,
                -68.8458,
                "SOLAR",
                100,
                150000,
                21.5,
                "completed",
                LocalDateTime.parse("2026-06-08T09:00:00"));
        SimulationHistoryResultDTO second = new SimulationHistoryResultDTO(
                11L,
                "Wind Two",
                "Cordoba",
                "AR",
                -31.4167,
                -64.1833,
                "WIND",
                80,
                120000,
                18.0,
                "completed",
                LocalDateTime.parse("2026-06-08T10:00:00"));

        when(historyUseCase.getUserHistory("alice")).thenReturn(List.of(first, second));
        when(responseMapper.toUserSummary(first)).thenReturn(new UserSimulationSummaryDTO(
                10L,
                "Solar One",
                "solar",
                100,
                150000,
                21.5,
                "completed",
                LocalDateTime.parse("2026-06-08T09:00:00"),
                new SimulationLocationSummaryDTO("Mendoza", "AR")));
        when(responseMapper.toUserSummary(second)).thenReturn(new UserSimulationSummaryDTO(
                11L,
                "Wind Two",
                "wind",
                80,
                120000,
                18.0,
                "completed",
                LocalDateTime.parse("2026-06-08T10:00:00"),
                new SimulationLocationSummaryDTO("Cordoba", "AR")));

        PageResponse<UserSimulationSummaryDTO> response = controller
                .getMySimulations(auth, 0, 1, "completed")
                .getBody();

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.last()).isFalse();
        assertThat(response.content().getFirst().name()).isEqualTo("Solar One");
    }

    @Test
    @DisplayName("get user simulations route does not collide with numeric id mapping")
    void getUserSimulationsRouteDoesNotCollideWithIdRoute() throws Exception {
        Authentication auth = authentication("alice", "ROLE_USER");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(historyUseCase.getUserHistory("alice")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/simulations/user")
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(historyUseCase).getUserHistory("alice");
        verify(getUseCase, never()).getSimulationById(any(GetSimulationByIdCommand.class));
    }

    @Test
    @DisplayName("reverseGeocode returns resolved location from coordinates")
    void reverseGeocodeReturnsResolvedLocation() {
        when(climateDataProviderPort.resolveLocation(-32.8895, -68.8458))
                .thenReturn(new ResolvedLocation("Mendoza", "AR", -32.8895, -68.8458));

        ResolvedLocationResponseDTO response = controller.reverseGeocode(-32.8895, -68.8458).getBody();

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Mendoza");
        assertThat(response.country()).isEqualTo("AR");
        assertThat(response.lat()).isEqualTo(-32.8895);
        assertThat(response.lon()).isEqualTo(-68.8458);
    }

    @Test
    @DisplayName("searchLocations returns backend location suggestions")
    void searchLocationsReturnsSuggestions() {
        when(climateDataProviderPort.searchLocations("mendoza", 5)).thenReturn(List.of(
                new ResolvedLocation("Mendoza", "AR", -32.8895, -68.8458),
                new ResolvedLocation("Mendoza City", "AR", -32.90, -68.84)));

        List<ResolvedLocationResponseDTO> response = controller.searchLocations("mendoza", 5).getBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
        assertThat(response.getFirst().name()).isEqualTo("Mendoza");
        assertThat(response.getFirst().country()).isEqualTo("AR");
    }

    @Test
    @DisplayName("getDashboardSummary returns aggregated dashboard payload")
    void getDashboardSummaryReturnsAggregatedPayload() {
        Authentication auth = authentication("alice", "ROLE_USER");

        when(dashboardSummaryUseCase.getDashboardSummary("alice")).thenReturn(
                new SimulationDashboardSummaryResult(
                        new SimulationDashboardStatsResult(2, 3000.0, 1200.0, 14.5),
                        List.of(new SimulationDashboardEnergyBySourceResult("Solar", 3000.0)),
                        List.of(),
                        List.of()));

        DashboardSummaryResponseDTO response = controller.getDashboardSummary(auth).getBody();

        assertThat(response).isNotNull();
        assertThat(response.stats().totalSimulations()).isEqualTo(2);
        assertThat(response.stats().totalEnergyGeneratedKwh()).isEqualTo(3000.0);
        assertThat(response.stats().totalCo2SavedKg()).isEqualTo(1200.0);
        assertThat(response.energyBySource()).hasSize(1);
        assertThat(response.energyBySource().getFirst().label()).isEqualTo("Solar");
        assertThat(response.efficiencyMetrics()).isEmpty();
        assertThat(response.targetVsActual()).isEmpty();
    }

    private Authentication authentication(String username, String role) {
        AuthenticatedUser user = AuthenticatedUser.of(username, Set.of(role.replace("ROLE_", "")), Set.of("read:simulations", "write:simulations"));
        return new TestingAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority(role)));
    }
}
