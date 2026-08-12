package com.renewsim.backend.simulation_service.create.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.Simulation;

public interface CreateSimulationRepositoryPort {

    Simulation save(Simulation simulation);
}
