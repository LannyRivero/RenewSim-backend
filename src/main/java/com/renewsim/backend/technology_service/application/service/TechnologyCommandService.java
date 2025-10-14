package com.renewsim.backend.technology_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.technology_service.application.command.CreateTechnologyCommand;
import com.renewsim.backend.technology_service.application.command.DeleteTechnologyCommand;
import com.renewsim.backend.technology_service.application.command.GetTechnologyByIdCommand;
import com.renewsim.backend.technology_service.application.command.UpdateTechnologyCommand;
import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.application.result.*;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.infrastructure.mapper.TechnologyDtoMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TechnologyCommandService {

    private final TechnologyRepositoryPort repository;
    private final TechnologyValidator validator;
    private final TechnologyDtoMapper dtoMapper;

    // ------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------
    public TechnologyCreationResultDTO handleCreate(CreateTechnologyCommand command) {
        validator.ensureUniqueName(command.name());

        Technology technology = toDomain(command);
        Technology saved = repository.save(technology);

        return dtoMapper.toCreationResult(saved);
    }

    // ------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------
    public TechnologyUpdateResultDTO handleUpdate(UpdateTechnologyCommand command) {
        validator.ensureExists(command.id());

        Technology updated = repository.save(toDomain(command));

        return dtoMapper.toUpdateResult(updated);
    }

    // ------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------
    public void handleDelete(DeleteTechnologyCommand command) {
        validator.ensureExists(command.id());
        repository.deleteById(command.id());
    }

    // ------------------------------------------------------------
    // QUERY (BY ID / ALL)
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public TechnologyQueryResultDTO handleGetById(GetTechnologyByIdCommand command) {
        Technology tech = validator.getExisting(command.id());
        return dtoMapper.toQueryResult(tech);
    }

    @Transactional(readOnly = true)
    public List<TechnologyQueryResultDTO> handleGetAll() {
        return repository.findAll()
                .stream()
                .map(dtoMapper::toQueryResult)
                .toList();
    }

    // ------------------------------------------------------------
    // DOMAIN CONSTRUCTORS
    // ------------------------------------------------------------
    private Technology toDomain(CreateTechnologyCommand c) {
        return new Technology(
                c.name(),
                EnergyType.valueOf(c.energyType().toUpperCase()),
                new Efficiency(c.efficiency()),
                new InstallationCost(BigDecimal.valueOf(c.installationCost())),
                new MaintenanceCost(BigDecimal.valueOf(c.maintenanceCost())),
                new EnvironmentalImpact(c.environmentalImpact()),
                new Co2Reduction(BigDecimal.valueOf(c.co2Reduction())),
                new EnergyProduction(c.energyProduction())
        );
    }

    private Technology toDomain(UpdateTechnologyCommand c) {
        return new Technology(
                c.name(),
                EnergyType.valueOf(c.energyType().toUpperCase()),
                new Efficiency(c.efficiency()),
                new InstallationCost(BigDecimal.valueOf(c.installationCost())),
                new MaintenanceCost(BigDecimal.valueOf(c.maintenanceCost())),
                new EnvironmentalImpact(c.environmentalImpact()),
                new Co2Reduction(BigDecimal.valueOf(c.co2Reduction())),
                new EnergyProduction(c.energyProduction())
        );
    }
}
