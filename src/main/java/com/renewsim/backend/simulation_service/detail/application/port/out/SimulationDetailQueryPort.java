package com.renewsim.backend.simulation_service.detail.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.Simulation;

import java.util.Optional;

public interface SimulationDetailQueryPort {

    Optional<Simulation> findById(Long id);
}
