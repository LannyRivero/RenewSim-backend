package com.renewsim.backend.simulation_service.detail.web;

import com.renewsim.backend.simulation_service.detail.application.port.in.GetRealSimulationUseCase;
import com.renewsim.backend.simulation_service.shared.web.SimulationDetailsWebMapper;
import com.renewsim.backend.simulation_service.shared.web.dto.SimulationDetailsResponseDTO;
import com.renewsim.backend.shared.security.AuthenticatedRequestContext;
import com.renewsim.backend.shared.security.AuthenticatedRequestContextFactory;
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
    private final SimulationDetailsWebMapper detailsWebMapper = new SimulationDetailsWebMapper();
    private final AuthenticatedRequestContextFactory requestContextFactory;

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    public ResponseEntity<SimulationDetailsResponseDTO> getSimulationById(
            @PathVariable Long id,
            Authentication auth) {
        AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);

        return ResponseEntity.ok(detailsWebMapper.toWebDetails(
                getRealSimulationUseCase.getSimulationById(
                        id,
                        requestContext.username(),
                        requestContext.isAdmin())));
    }
}
