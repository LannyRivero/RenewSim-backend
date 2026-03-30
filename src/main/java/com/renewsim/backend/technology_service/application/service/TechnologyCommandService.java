package com.renewsim.backend.technology_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.technology_service.application.command.*;
import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.application.result.*;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.application.mapper.TechnologyDtoMapper;
import com.renewsim.backend.technology_service.domain.factory.TechnologyFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application Service for handling technology lifecycle operations.
 * 
 * Follows CQRS principles:
 * - Commands mutate state (create, update, delete)
 * - Queries retrieve state (getById, getAll)
 * 
 * All inputs are validated records (commands) and outputs are immutable DTOs.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TechnologyCommandService {

    private final TechnologyRepositoryPort repository;
    private final TechnologyValidator validator;
    private final TechnologyDtoMapper dtoMapper;

    // ============================================================
    // ⛏️ Command Handlers
    // ============================================================

    public TechnologyCreationResultDTO handleCreate(CreateTechnologyCommand command) {
        validator.ensureUniqueName(command.name());

        Technology newTech = TechnologyFactory.create(
            command.name(),
            command.efficiency(),
            command.installationCost(),
            command.maintenanceCost(),
            command.environmentalImpact(),
            command.co2Reduction(),
            command.energyProduction(),
            command.energyType()
        );

        var saved = repository.save(newTech);
        return dtoMapper.toCreationResult(saved);
    }

    public TechnologyUpdateResultDTO handleUpdate(UpdateTechnologyCommand command) {
        var existing = validator.getExisting(command.id());

        var updated = new Technology(
                existing.getId(),
                command.name(),
                EnergyType.valueOf(command.energyType().toUpperCase()),
                new Efficiency(command.efficiency()),
                new InstallationCost(BigDecimal.valueOf(command.installationCost())),
                new MaintenanceCost(BigDecimal.valueOf(command.maintenanceCost())),
                new EnvironmentalImpact(command.environmentalImpact()),
                new Co2Reduction(BigDecimal.valueOf(command.co2Reduction())),
                new EnergyProduction(command.energyProduction()));

        repository.save(updated);
        return dtoMapper.toUpdateResult(updated);
    }

    public void handleDelete(DeleteTechnologyCommand command) {
        validator.ensureExists(command.id());
        repository.deleteById(command.id());
    }

    // ============================================================
    // 🔍 Query Handlers
    // ============================================================

    @Transactional(readOnly = true)
    public TechnologyQueryResultDTO handleGetById(GetTechnologyByIdCommand command) {
        var tech = validator.getExisting(command.id());
        return dtoMapper.toQueryResult(tech);
    }

    @Transactional(readOnly = true)
    public List<TechnologyQueryResultDTO> handleGetAll() {
        return repository.findAll().stream()
                .map(dtoMapper::toQueryResult)
                .toList();
    }

   
}
