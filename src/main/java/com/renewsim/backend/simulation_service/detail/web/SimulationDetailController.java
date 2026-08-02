package com.renewsim.backend.simulation_service.detail.web;

import com.renewsim.backend.simulation_service.application.detailSimulation.GetRealSimulationUseCase;
import com.renewsim.backend.simulation_service.shared.web.SimulationRequestContext;
import com.renewsim.backend.simulation_service.shared.web.SimulationRequestContextFactory;
import com.renewsim.backend.simulation_service.detail.web.dto.SimulationDetailsResponseDTO;
import com.renewsim.backend.simulation_service.web.controller.SimulationWebMapper;
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
public class SimulationDetailController {

    private final GetRealSimulationUseCase getRealSimulationUseCase;
    private final SimulationWebMapper webMapper = new SimulationWebMapper();
    private final SimulationRequestContextFactory requestContextFactory = new SimulationRequestContextFactory();

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    public ResponseEntity<SimulationDetailsResponseDTO> getSimulationById(
            @PathVariable Long id,
            Authentication auth) {
        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        return ResponseEntity.ok(webMapper.toWebDetails(
                getRealSimulationUseCase.getSimulationById(
                        id,
                        requestContext.username(),
                        requestContext.isAdmin())));
    }
}
