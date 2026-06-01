package com.renewsim.backend.technology_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
            command.capacityFactor(),
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
                EnergyType.fromString(command.energyType()),
                new Efficiency(command.efficiency()),
                new InstallationCost(BigDecimal.valueOf(command.installationCost())),
                new MaintenanceCost(BigDecimal.valueOf(command.maintenanceCost())),
                new EnvironmentalImpact(command.environmentalImpact()),
                new Co2Reduction(BigDecimal.valueOf(command.co2Reduction())),
                new CapacityFactor(command.capacityFactor()));

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
    public TechnologyResponseDTO handleGetById(GetTechnologyByIdCommand command) {
        var tech = validator.getExisting(command.id());
        return dtoMapper.toResponse(tech);
    }

    @Transactional(readOnly = true)
    public Page<TechnologyResponseDTO> handleGetAll(int page, int size, String energyType) {
        var pageable = PageRequest.of(page, size);
        if (energyType != null && !energyType.isBlank()) {
            return repository.findByEnergyType(EnergyType.fromString(energyType), pageable)
                    .map(dtoMapper::toResponse);
        }
        return repository.findAllActive(pageable)
                .map(dtoMapper::toResponse);
    }

   
}
