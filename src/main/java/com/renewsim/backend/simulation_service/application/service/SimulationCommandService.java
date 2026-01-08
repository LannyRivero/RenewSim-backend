package com.renewsim.backend.simulation_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.renewsim.backend.simulation_service.application.port.out.SimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;

@Service
@RequiredArgsConstructor
public class SimulationCommandService {

    private final SimulationRepositoryPort repository;

    public Simulation save(Simulation simulation) {
        return repository.save(simulation);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}

