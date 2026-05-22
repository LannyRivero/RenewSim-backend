package com.renewsim.backend.simulation_service.web.controller;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.simulation_service.application.command.GetSimulationByIdCommand;
import com.renewsim.backend.simulation_service.application.port.in.GetSimulationUseCase;
import com.renewsim.backend.simulation_service.application.port.out.TechnologyClientPort;
import com.renewsim.backend.simulation_service.application.result.SimulationDetailResultDTO;
import com.renewsim.backend.simulation_service.web.dto.TechnologyResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationTechnologyControllerTest {

    @Mock
    private GetSimulationUseCase getSimulationUseCase;
    @Mock
    private TechnologyClientPort technologyClientPort;

    @InjectMocks
    private SimulationTechnologyController controller;

    @Test
    @DisplayName("RED->GREEN: technologies endpoint returns concrete technology data for simulation IDs")
    void getTechnologiesBySimulationIdReturnsMappedTechnologyPayload() {
        AuthenticatedUser user = AuthenticatedUser.of(
                "alice",
                Set.of("USER"),
                Set.of("read:simulations"));
        Authentication authentication = new TestingAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SimulationDetailResultDTO simulation = new SimulationDetailResultDTO(
                55L,
                "Mendoza",
                "SOLAR",
                100,
                10000,
                120000,
                30000,
                4.0,
                LocalDateTime.parse("2026-05-22T11:00:00"),
                "alice",
                List.of(1L, 2L));

        when(getSimulationUseCase.getSimulationById(any(GetSimulationByIdCommand.class))).thenReturn(simulation);
        when(technologyClientPort.getTechnologyById(1L)).thenReturn(new TechnologyResponseDTO(1L, "Solar Panel", "SOLAR", 21, 20000, 30, 5000));
        when(technologyClientPort.getTechnologyById(2L)).thenReturn(new TechnologyResponseDTO(2L, "Wind Turbine", "WIND", 35, 50000, 50, 12000));

        List<TechnologyResponseDTO> response = controller.getTechnologiesBySimulationId(55L, authentication);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).id()).isEqualTo(1L);
        assertThat(response.get(1).id()).isEqualTo(2L);
        verify(technologyClientPort, times(2)).getTechnologyById(any(Long.class));
    }

    @Test
    @DisplayName("TRIANGULATE: technologies endpoint keeps ownership command context")
    void getTechnologiesBySimulationIdUsesOwnerAndAdminFlags() {
        AuthenticatedUser user = AuthenticatedUser.of(
                "admin",
                Set.of("ADMIN"),
                Set.of("read:simulations"));
        Authentication authentication = new TestingAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        SimulationDetailResultDTO simulation = new SimulationDetailResultDTO(
                88L,
                "Cordoba",
                "WIND",
                50,
                7000,
                60000,
                11000,
                2.0,
                LocalDateTime.parse("2026-05-22T12:00:00"),
                "alice",
                List.of());

        when(getSimulationUseCase.getSimulationById(any(GetSimulationByIdCommand.class))).thenReturn(simulation);

        controller.getTechnologiesBySimulationId(88L, authentication);

        ArgumentCaptor<GetSimulationByIdCommand> commandCaptor = ArgumentCaptor.forClass(GetSimulationByIdCommand.class);
        verify(getSimulationUseCase).getSimulationById(commandCaptor.capture());
        assertThat(commandCaptor.getValue().requesterUsername()).isEqualTo("admin");
        assertThat(commandCaptor.getValue().isAdmin()).isTrue();
    }
}
