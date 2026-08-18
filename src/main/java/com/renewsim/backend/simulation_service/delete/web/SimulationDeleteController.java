package com.renewsim.backend.simulation_service.delete.web;

import com.renewsim.backend.simulation_service.delete.application.port.in.DeleteRealSimulationUseCase;
import com.renewsim.backend.shared.security.AuthenticatedRequestContext;
import com.renewsim.backend.shared.security.AuthenticatedRequestContextFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Simulation Delete API", description = "Soft-delete simulations of the authenticated user")
public class SimulationDeleteController {

    private final DeleteRealSimulationUseCase deleteRealSimulationUseCase;
    private final AuthenticatedRequestContextFactory requestContextFactory;

    @Operation(summary = "Delete simulation by ID", description = "Soft-deletes a single simulation. Only the owner or an admin can delete it.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Simulation deleted", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not the owner and not admin", content = @Content),
            @ApiResponse(responseCode = "404", description = "Simulation not found", content = @Content)
    })
    @PreAuthorize("hasAuthority('SCOPE_delete:simulations') or hasRole('ADMIN')")
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteSimulation(
            @Parameter(description = "Simulation unique identifier", required = true) @PathVariable Long id,
            Authentication auth) {

        AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);

        deleteRealSimulationUseCase.deleteSimulation(id, requestContext.username(), requestContext.isAdmin());
        log.warn("User {} deleted simulation {}", requestContext.username(), id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all simulations of authenticated user", description = "Soft-deletes every simulation owned by the authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Simulations deleted", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    @PreAuthorize("hasAuthority('SCOPE_delete:simulations') or hasRole('ADMIN')")
    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteAllUserSimulations(Authentication auth) {
        AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);

        deleteRealSimulationUseCase.deleteAllUserSimulations(requestContext.username());
        log.warn("User {} deleted all simulations", requestContext.username());

        return ResponseEntity.noContent().build();
    }
}
