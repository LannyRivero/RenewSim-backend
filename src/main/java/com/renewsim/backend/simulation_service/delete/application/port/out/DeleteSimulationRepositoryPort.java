package com.renewsim.backend.simulation_service.delete.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.Simulation;

import java.util.Optional;

public interface DeleteSimulationRepositoryPort {

    Optional<Simulation> findById(Long id);

    void deleteById(Long id);

    void deleteAllByCreatedBy(String createdBy);
}
