package com.renewsim.backend.simulation_service.create.application.technology.wind;

import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.create.application.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.SimulationEngine;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationTechnologyException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import org.springframework.stereotype.Component;

@Component
public class WindSimulationEngine implements SimulationEngine {

    @Override
    public boolean supports(Technology technology) {
        return "wind".equals(technology.value());
    }

    @Override
    public void assertImplemented() {
        throw new InvalidSimulationTechnologyException("UNSUPPORTED_TECHNOLOGY: 'wind' simulation is not implemented yet");
    }

    @Override
    public SimulationDetailsResult simulate(Simulation simulation, CreateRealSimulationCommand command) {
        throw new IllegalStateException("Wind simulation should not execute before implementation is available.");
    }
}
