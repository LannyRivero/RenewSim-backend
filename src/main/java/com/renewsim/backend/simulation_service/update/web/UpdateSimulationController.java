package com.renewsim.backend.simulation_service.update.web;

import com.renewsim.backend.simulation_service.create.web.dto.CreateSimulationRequestDTO;
import com.renewsim.backend.simulation_service.shared.web.SimulationDetailsWebMapper;
import com.renewsim.backend.simulation_service.shared.web.dto.SimulationDetailsResponseDTO;
import com.renewsim.backend.simulation_service.update.application.port.in.UpdateSimulationUseCase;
import com.renewsim.backend.shared.security.AuthenticatedRequestContext;
import com.renewsim.backend.shared.security.AuthenticatedRequestContextFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
@Tag(name = "Simulation Update API", description = "Edit renewable energy simulations")
public class UpdateSimulationController {

    private final UpdateSimulationUseCase updateSimulationUseCase;
    private final UpdateSimulationWebMapper updateWebMapper = new UpdateSimulationWebMapper();
    private final SimulationDetailsWebMapper detailsWebMapper = new SimulationDetailsWebMapper();
    private final AuthenticatedRequestContextFactory requestContextFactory;

    @Operation(summary = "Update a simulation", description = "Edits an existing simulation owned by the caller and recomputes its results. Only simulations that are not deleted can be updated.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation updated", content = @Content(schema = @Schema(implementation = SimulationDetailsResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not the owner and not admin, or missing write scope", content = @Content),
            @ApiResponse(responseCode = "404", description = "Simulation not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Simulation is in a terminal state and cannot be updated", content = @Content)
    })
    @PreAuthorize("hasAuthority('SCOPE_write:simulations') or hasRole('ADMIN')")
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<SimulationDetailsResponseDTO> updateSimulation(
            @Parameter(description = "Simulation unique identifier", required = true) @PathVariable Long id,
            @Valid @RequestBody CreateSimulationRequestDTO request,
            Authentication auth) {
        AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);

        SimulationDetailsResponseDTO result = detailsWebMapper.toWebDetails(
                updateSimulationUseCase.updateSimulation(
                        updateWebMapper.toCommand(id, request),
                        requestContext.username(),
                        requestContext.isAdmin()));
        log.info("User {} updated simulation {}", requestContext.username(), id);

        return ResponseEntity.ok(result);
    }
}