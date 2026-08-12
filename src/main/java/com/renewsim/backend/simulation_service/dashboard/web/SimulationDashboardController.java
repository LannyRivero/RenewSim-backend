package com.renewsim.backend.simulation_service.dashboard.web;

import com.renewsim.backend.simulation_service.dashboard.application.port.in.GetPortfolioDashboardUseCase;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardResponseDTO;
import com.renewsim.backend.shared.security.AuthenticatedRequestContext;
import com.renewsim.backend.shared.security.AuthenticatedRequestContextFactory;
import io.swagger.v3.oas.annotations.Operation;
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
public class SimulationDashboardController {

    private final GetPortfolioDashboardUseCase getPortfolioDashboardUseCase;
    private final SimulationDashboardWebMapper dashboardWebMapper = new SimulationDashboardWebMapper();
    private final AuthenticatedRequestContextFactory requestContextFactory;

    @Operation(summary = "Get executive portfolio dashboard for authenticated user")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<PortfolioDashboardResponseDTO> getDashboard(Authentication auth) {
        AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);

        return ResponseEntity
                .ok(dashboardWebMapper.toWebDashboard(getPortfolioDashboardUseCase.getDashboard(requestContext.username())));
    }
}
