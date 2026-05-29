package com.renewsim.backend.simulation_service.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.Simulation;

import java.util.List;

public interface TechnologyRecommendationPort {

    List<Long> recommendFor(Simulation simulation);
}
