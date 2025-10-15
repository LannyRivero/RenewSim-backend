package com.renewsim.backend.technology_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.technology_service.application.command.*;
import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.application.result.*;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.infrastructure.mapper.TechnologyDtoMapper;

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
        var saved = repository.save(toDomain(command));
        return dtoMapper.toCreationResult(saved);
    }

    public TechnologyUpdateResultDTO handleUpdate(UpdateTechnologyCommand command) {
        validator.ensureExists(command.id());
        var updated = repository.save(toDomain(command));
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

    // ============================================================
    // 🧩 Domain Conversion Helpers
    // ============================================================

    private Technology toDomain(CreateTechnologyCommand c) {
        return new Technology(
                c.name(),
                EnergyType.valueOf(c.energyType().toUpperCase()),
                new Efficiency(c.efficiency()),
                new InstallationCost(BigDecimal.valueOf(c.installationCost())),
                new MaintenanceCost(BigDecimal.valueOf(c.maintenanceCost())),
                new EnvironmentalImpact(c.environmentalImpact()),
                new Co2Reduction(BigDecimal.valueOf(c.co2Reduction())),
                new EnergyProduction(c.energyProduction()));
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
                new EnergyProduction(c.energyProduction()));
    }
}
