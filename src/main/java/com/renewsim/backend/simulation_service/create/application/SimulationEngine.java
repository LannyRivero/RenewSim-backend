package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;

public interface SimulationEngine {
    boolean supports(Technology technology);

    default void assertImplemented() {
    }

    SimulationDetailsResult simulate(Simulation simulation, CreateRealSimulationCommand command);
}
