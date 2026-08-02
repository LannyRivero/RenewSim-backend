package com.renewsim.backend.simulation_service.history.web;

import com.renewsim.backend.simulation_service.application.deleteSimulation.DeleteRealSimulationUseCase;
import com.renewsim.backend.simulation_service.application.historySimulation.ListUserRealSimulationsUseCase;
import com.renewsim.backend.simulation_service.shared.web.SimulationRequestContext;
import com.renewsim.backend.simulation_service.shared.web.SimulationRequestContextFactory;
import com.renewsim.backend.simulation_service.history.web.dto.ListUserSimulationsResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
public class SimulationHistoryController {

    private final ListUserRealSimulationsUseCase listUserRealSimulationsUseCase;
    private final DeleteRealSimulationUseCase deleteRealSimulationUseCase;
    private final SimulationHistoryWebMapper historyWebMapper = new SimulationHistoryWebMapper();
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

    @Operation(summary = "Get authenticated user simulations with pagination")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping({ "/user", "/my-simulations" })
    public ResponseEntity<ListUserSimulationsResponseDTO> getMySimulations(Authentication auth) {
        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        ListUserSimulationsResponseDTO history = historyWebMapper.toWebList(
                listUserRealSimulationsUseCase.getUserSimulations(requestContext.username()));
        log.info("User {} retrieved {} simulations", requestContext.username(), history.total());

        return ResponseEntity.ok(history);
    }
}
