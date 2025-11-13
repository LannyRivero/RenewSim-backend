package com.renewsim.backend.simulation_service.infrastructure.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.renewsim.backend.simulation_service.application.port.in.GetSimulationUseCase;
import com.renewsim.backend.simulation_service.application.command.GetSimulationByIdCommand;
import com.renewsim.backend.simulation_service.application.result.SimulationQueryResultDTO;

import java.util.List;

@RestController
@RequestMapping("/api/v1/simulation")
@RequiredArgsConstructor
public class SimulationTechnologyController {

    private final GetSimulationUseCase getSimulationUseCase;

    /**
     * Devuelve las tecnologías asociadas a una simulación específica
     * Ejemplo: GET /api/v1/simulation/6/technologies
     */
    @GetMapping("/{id}/technologies")
    public List<String> getTechnologiesBySimulationId(@PathVariable Long id) {
        SimulationQueryResultDTO simulation = getSimulationUseCase.getSimulationById(new GetSimulationByIdCommand(id));

        return simulation.technologyIds();
    }
}
