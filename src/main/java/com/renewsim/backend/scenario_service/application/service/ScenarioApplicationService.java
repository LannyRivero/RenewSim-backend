package com.renewsim.backend.scenario_service.application.service;

import com.renewsim.backend.scenario_service.application.command.CreateScenarioCommand;
import com.renewsim.backend.scenario_service.application.command.GetScenarioByIdCommand;
import com.renewsim.backend.scenario_service.application.command.UpdateScenarioCommand;
import com.renewsim.backend.scenario_service.application.mapper.ScenarioDtoMapper;
import com.renewsim.backend.scenario_service.application.port.in.CreateScenarioUseCase;
import com.renewsim.backend.scenario_service.application.port.in.GetScenarioUseCase;
import com.renewsim.backend.scenario_service.application.port.in.UpdateScenarioUseCase;
import com.renewsim.backend.scenario_service.application.port.out.ScenarioRepositoryPort;
import com.renewsim.backend.scenario_service.application.result.ScenarioResponseDTO;
import com.renewsim.backend.scenario_service.domain.exception.ScenarioNotFoundException;
import com.renewsim.backend.scenario_service.domain.model.Scenario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScenarioApplicationService implements GetScenarioUseCase, CreateScenarioUseCase, UpdateScenarioUseCase {

    private final ScenarioRepositoryPort repository;
    private final ScenarioDtoMapper dtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ScenarioResponseDTO> getAllActiveScenarios() {
        return repository.findAllActive().stream().map(dtoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ScenarioResponseDTO getScenarioById(GetScenarioByIdCommand command) {
        Scenario scenario = repository.findById(command.id())
                .filter(Scenario::isActive)
                .orElseThrow(() -> new ScenarioNotFoundException(command.id()));
        return dtoMapper.toResponse(scenario);
    }

    @Override
    public ScenarioResponseDTO createScenario(CreateScenarioCommand command) {
        Scenario scenario = new Scenario(
                command.name(),
                command.description(),
                command.technologyId().value(),
                command.defaultCapacityKw().value(),
                command.defaultInvestment(),
                command.defaultTariff().value(),
                command.defaultConsumption().value(),
                command.climateProfile());
        return dtoMapper.toResponse(repository.save(scenario));
    }

    @Override
    public ScenarioResponseDTO updateScenario(UpdateScenarioCommand command) {
        Scenario existing = repository.findById(command.id())
                .filter(Scenario::isActive)
                .orElseThrow(() -> new ScenarioNotFoundException(command.id()));

        Scenario updated = new Scenario(
                existing.getId(),
                command.name(),
                command.description(),
                command.technologyId(),
                command.defaultCapacityKw(),
                command.defaultInvestment(),
                command.defaultTariff(),
                command.defaultConsumption(),
                command.climateProfile(),
                existing.isActive());

        return dtoMapper.toResponse(repository.save(updated));
    }
}
