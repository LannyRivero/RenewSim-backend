package com.renewsim.backend.simulation_service.web.controller;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.simulation_service.application.port.in.GetSimulationUseCase;
import com.renewsim.backend.simulation_service.application.command.GetSimulationByIdCommand;
import com.renewsim.backend.simulation_service.application.detailSimulation.SimulationDetailResultDTO;
import com.renewsim.backend.simulation_service.application.port.out.TechnologyClientPort;
import com.renewsim.backend.simulation_service.web.dto.TechnologyResponseDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/simulation")
@RequiredArgsConstructor
public class SimulationTechnologyController {

    private final GetSimulationUseCase getSimulationUseCase;
    private final TechnologyClientPort technologyClientPort;

    /**
     * Devuelve las tecnologías asociadas a una simulación específica
     * Ejemplo: GET /api/v1/simulation/6/technologies
     */
    @PreAuthorize("hasAuthority('SCOPE_read:simulations')")
    @GetMapping("/{id}/technologies")
    public List<TechnologyResponseDTO> getTechnologiesBySimulationId(@PathVariable Long id, Authentication auth) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        SimulationDetailResultDTO simulation = getSimulationUseCase.getSimulationById(
            new GetSimulationByIdCommand(id, user.username(), isAdmin)
        );

        return simulation.technologyIds().stream()
                .map(technologyClientPort::getTechnologyById)
                .toList();
    }
}
