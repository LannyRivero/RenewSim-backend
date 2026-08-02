package com.renewsim.backend.simulation_service.dashboard.web;

import com.renewsim.backend.simulation_service.application.dashboard.GetPortfolioDashboardUseCase;
import com.renewsim.backend.simulation_service.shared.web.SimulationRequestContext;
import com.renewsim.backend.simulation_service.shared.web.SimulationRequestContextFactory;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardResponseDTO;
import com.renewsim.backend.simulation_service.web.controller.SimulationWebMapper;
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
    private final SimulationWebMapper webMapper = new SimulationWebMapper();
    private final SimulationRequestContextFactory requestContextFactory = new SimulationRequestContextFactory();

    @Operation(summary = "Get executive portfolio dashboard for authenticated user")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<PortfolioDashboardResponseDTO> getDashboard(Authentication auth) {
        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        return ResponseEntity
                .ok(webMapper.toWebDashboard(getPortfolioDashboardUseCase.getDashboard(requestContext.username())));
    }
}
