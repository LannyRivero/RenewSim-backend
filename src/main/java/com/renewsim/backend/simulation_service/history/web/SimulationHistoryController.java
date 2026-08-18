package com.renewsim.backend.simulation_service.history.web;

import com.renewsim.backend.simulation_service.history.application.port.in.ListUserRealSimulationsUseCase;
import com.renewsim.backend.simulation_service.history.web.dto.ListUserSimulationsResponseDTO;
import com.renewsim.backend.shared.security.AuthenticatedRequestContext;
import com.renewsim.backend.shared.security.AuthenticatedRequestContextFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
@Tag(name = "Simulation History API", description = "List the simulations of the authenticated user")
public class SimulationHistoryController {

    private final ListUserRealSimulationsUseCase listUserRealSimulationsUseCase;
    private final SimulationHistoryWebMapper historyWebMapper = new SimulationHistoryWebMapper();
    private final AuthenticatedRequestContextFactory requestContextFactory;

    @Operation(summary = "Get authenticated user simulations", description = "Returns the list of simulations owned by the authenticated user with their current status and recommendation.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation list retrieved", content = @Content(schema = @Schema(implementation = ListUserSimulationsResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping({ "/user", "/my-simulations" })
    public ResponseEntity<ListUserSimulationsResponseDTO> getMySimulations(Authentication auth) {
        AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);

        ListUserSimulationsResponseDTO history = historyWebMapper.toWebList(
                listUserRealSimulationsUseCase.getUserSimulations(requestContext.username()));
        log.info("User {} retrieved {} simulations", requestContext.username(), history.total());

        return ResponseEntity.ok(history);
    }
}
