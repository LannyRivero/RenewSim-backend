package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;

/**
 * Strategy contract for the physics-based simulation engines of {@code simulation_service}.
 *
 * <p>Each engine owns the computation for a single energy type (solar, wind, hydro) and is
 * selected at runtime through {@link #supports(Technology)}. Engines deliberately share the
 * {@code CreateRealSimulationCommand} input contract with the create and update use cases so
 * that a re-simulation after an edit produces results comparable to the original creation.</p>
 */
public interface SimulationEngine {
    boolean supports(Technology technology);

    default void assertImplemented() {
    }

    SimulationDetailsResult simulate(Simulation simulation, CreateRealSimulationCommand command);
}
