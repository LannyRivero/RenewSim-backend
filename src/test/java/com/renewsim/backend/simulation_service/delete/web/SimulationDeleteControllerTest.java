package com.renewsim.backend.simulation_service.delete.web;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.shared.security.AuthenticatedRequestContext;
import com.renewsim.backend.shared.security.AuthenticatedRequestContextFactory;
import com.renewsim.backend.simulation_service.delete.application.port.in.DeleteRealSimulationUseCase;
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

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SimulationDeleteControllerTest {

    @Mock
    private DeleteRealSimulationUseCase deleteRealSimulationUseCase;
    @Mock
    private AuthenticatedRequestContextFactory requestContextFactory;

    @InjectMocks
    private SimulationDeleteController controller;

    @Test
    @DisplayName("deleteSimulation delegates to delete use case with request context")
    void deleteSimulationDelegatesToDeleteUseCaseWithRequestContext() {
        Authentication auth = authentication("alice", "ROLE_USER");
        org.mockito.Mockito.when(requestContextFactory.from(auth)).thenReturn(new AuthenticatedRequestContext("alice", false));

        controller.deleteSimulation(55L, auth);

        verify(deleteRealSimulationUseCase).deleteSimulation(55L, "alice", false);
    }

    @Test
    @DisplayName("deleteAllUserSimulations delegates to delete use case with current username")
    void deleteAllUserSimulationsDelegatesToDeleteUseCaseWithCurrentUsername() {
        Authentication auth = authentication("alice", "ROLE_USER");
        org.mockito.Mockito.when(requestContextFactory.from(auth)).thenReturn(new AuthenticatedRequestContext("alice", false));

        controller.deleteAllUserSimulations(auth);

        verify(deleteRealSimulationUseCase).deleteAllUserSimulations("alice");
    }

    private Authentication authentication(String username, String role) {
        AuthenticatedUser user = AuthenticatedUser.of(username, Set.of(role.replace("ROLE_", "")),
                Set.of("read:simulations", "delete:simulations"));
        return new TestingAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority(role)));
    }
}
