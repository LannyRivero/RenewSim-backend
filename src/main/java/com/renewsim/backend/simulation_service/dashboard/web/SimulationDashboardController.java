package com.renewsim.backend.simulation_service.dashboard.web;

import com.renewsim.backend.simulation_service.dashboard.application.port.in.GetPortfolioDashboardUseCase;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardResponseDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
@Tag(name = "Simulation Dashboard API", description = "Executive portfolio overview for the authenticated user")
public class SimulationDashboardController {

    private final GetPortfolioDashboardUseCase getPortfolioDashboardUseCase;
    private final SimulationDashboardWebMapper dashboardWebMapper = new SimulationDashboardWebMapper();
    private final AuthenticatedRequestContextFactory requestContextFactory;

    @Operation(summary = "Get executive portfolio dashboard", description = "Returns the aggregated portfolio summary of the authenticated user: distribution by status and technology, prioritized scenarios, risk alerts and recommended next action.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio dashboard retrieved", content = @Content(schema = @Schema(implementation = PortfolioDashboardResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<PortfolioDashboardResponseDTO> getDashboard(Authentication auth) {
        AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);

        return ResponseEntity
                .ok(dashboardWebMapper.toWebDashboard(getPortfolioDashboardUseCase.getDashboard(requestContext.username())));
    }
}
