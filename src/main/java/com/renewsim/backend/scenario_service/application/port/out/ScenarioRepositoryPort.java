package com.renewsim.backend.scenario_service.application.port.out;

import com.renewsim.backend.scenario_service.domain.model.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepositoryPort {

    Scenario save(Scenario scenario);

    Optional<Scenario> findById(Long id);

    List<Scenario> findAllActive();
}
