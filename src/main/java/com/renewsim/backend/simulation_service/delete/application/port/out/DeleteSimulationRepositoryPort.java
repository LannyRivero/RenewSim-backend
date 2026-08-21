package com.renewsim.backend.simulation_service.delete.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.Simulation;

import java.util.List;
import java.util.Optional;

public interface DeleteSimulationRepositoryPort {

    Optional<Simulation> findById(Long id);

    List<Simulation> findActiveByCreatedBy(String createdBy);

    Simulation save(Simulation simulation);
}
