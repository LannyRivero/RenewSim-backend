package com.renewsim.backend.simulation_service.application.port.out;

import java.util.List;
import java.util.Optional;
import com.renewsim.backend.simulation_service.domain.model.Simulation;

public interface SimulationRepositoryPort {
    Simulation save(Simulation simulation);
    Optional<Simulation> findById(Long id);
    List<Simulation> findAllByCreatedBy(String username);
    Optional<Simulation> findDuplicate(String username, String name, String energyType, double latitude, double longitude);
    void deleteById(Long id);
    void deleteAllByCreatedBy(String username);
}