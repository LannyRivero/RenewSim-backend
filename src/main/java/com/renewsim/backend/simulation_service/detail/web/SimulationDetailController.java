package com.renewsim.backend.simulation_service.detail.web;

import com.renewsim.backend.simulation_service.detail.application.port.in.GetRealSimulationUseCase;
import com.renewsim.backend.simulation_service.shared.web.SimulationDetailsWebMapper;
import com.renewsim.backend.simulation_service.shared.web.dto.SimulationDetailsResponseDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
@Tag(name = "Simulation Detail API", description = "Retrieve the full contract of a renewable energy simulation")
public class SimulationDetailController {

    private final GetRealSimulationUseCase getRealSimulationUseCase;
    private final SimulationDetailsWebMapper detailsWebMapper = new SimulationDetailsWebMapper();
    private final AuthenticatedRequestContextFactory requestContextFactory;

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @Operation(summary = "Get simulation by ID", description = "Returns the complete simulation contract including inputs, computed results, financial summary and recommendation. Only the owner or an admin can read it.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation found", content = @Content(schema = @Schema(implementation = SimulationDetailsResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not the owner and not admin", content = @Content),
            @ApiResponse(responseCode = "404", description = "Simulation not found", content = @Content)
    })
    public ResponseEntity<SimulationDetailsResponseDTO> getSimulationById(
            @Parameter(description = "Simulation unique identifier", required = true) @PathVariable Long id,
            Authentication auth) {
        AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);

        return ResponseEntity.ok(detailsWebMapper.toWebDetails(
                getRealSimulationUseCase.getSimulationById(
                        id,
                        requestContext.username(),
                        requestContext.isAdmin())));
    }
}
