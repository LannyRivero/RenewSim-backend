package com.renewsim.backend.simulation_service.shared.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.Simulation;

import java.util.List;
import java.util.Optional;

public interface SimulationRecordRepositoryPort {

    Simulation save(Simulation simulation);

    Optional<Simulation> findById(Long id);

    List<Simulation> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    void deleteById(Long id);

    void deleteAllByCreatedBy(String createdBy);
}
