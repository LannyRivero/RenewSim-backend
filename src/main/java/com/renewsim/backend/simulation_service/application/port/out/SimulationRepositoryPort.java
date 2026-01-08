package com.renewsim.backend.simulation_service.application.port.out;

import java.util.List;
import java.util.Optional;
import com.renewsim.backend.simulation_service.domain.model.Simulation;

public interface SimulationRepositoryPort {
    Simulation save(Simulation simulation);
    Optional<Simulation> findById(Long id);
     List<Simulation> findAllByCreatedBy(String username);
    void deleteById(Long id);
}

