package com.renewsim.backend.simulation_service.delete.web;

import com.renewsim.backend.simulation_service.delete.application.port.in.DeleteRealSimulationUseCase;
import com.renewsim.backend.simulation_service.shared.web.SimulationRequestContext;
import com.renewsim.backend.simulation_service.shared.web.SimulationRequestContextFactory;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
public class SimulationDeleteController {

    private final DeleteRealSimulationUseCase deleteRealSimulationUseCase;
    private final SimulationRequestContextFactory requestContextFactory = new SimulationRequestContextFactory();

    @Operation(summary = "Delete simulation by ID")
    @PreAuthorize("hasAuthority('SCOPE_delete:simulations') or hasRole('ADMIN')")
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteSimulation(
            @PathVariable Long id,
            Authentication auth) {

        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        deleteRealSimulationUseCase.deleteSimulation(id, requestContext.username(), requestContext.isAdmin());
        log.warn("User {} deleted simulation {}", requestContext.username(), id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all simulations of authenticated user")
    @PreAuthorize("hasAuthority('SCOPE_delete:simulations') or hasRole('ADMIN')")
    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteAllUserSimulations(Authentication auth) {
        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        deleteRealSimulationUseCase.deleteAllUserSimulations(requestContext.username());
        log.warn("User {} deleted all simulations", requestContext.username());

        return ResponseEntity.noContent().build();
    }
}
