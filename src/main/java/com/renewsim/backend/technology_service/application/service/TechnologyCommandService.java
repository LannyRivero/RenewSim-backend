package com.renewsim.backend.technology_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.technology_service.application.command.CreateTechnologyCommand;
import com.renewsim.backend.technology_service.application.command.DeleteTechnologyCommand;
import com.renewsim.backend.technology_service.application.command.GetTechnologyByIdCommand;
import com.renewsim.backend.technology_service.application.command.UpdateTechnologyCommand;
import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.application.result.TechnologyCreationResultDTO;
import com.renewsim.backend.technology_service.application.result.TechnologyQueryResultDTO;
import com.renewsim.backend.technology_service.application.result.TechnologyUpdateResultDTO;
import com.renewsim.backend.technology_service.domain.model.Technology;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TechnologyCommandService {

    private final TechnologyRepositoryPort repository;
    private final TechnologyValidator validator;

    public TechnologyCreationResultDTO handleCreate(CreateTechnologyCommand command) {
        validator.ensureUniqueName(command.name());
        Technology saved = repository.save(toDomain(command));

        return new TechnologyCreationResultDTO(
                null, saved.name(), saved.energyType(),
                saved.efficiency(), saved.installationCost(),
                saved.maintenanceCost(), saved.environmentalImpact(),
                saved.co2Reduction(), saved.energyProduction(),
                true, "Technology created successfully");
    }

    public TechnologyUpdateResultDTO handleUpdate(UpdateTechnologyCommand command) {
        validator.ensureExists(command.id());
        Technology updated = repository.save(toDomain(command));

        return new TechnologyUpdateResultDTO(
                command.id(), updated.name(), updated.energyType(),
                updated.efficiency(), updated.installationCost(),
                updated.maintenanceCost(), updated.environmentalImpact(),
                updated.co2Reduction(), updated.energyProduction(),
                true, "Technology updated successfully");
    }

    public void handleDelete(DeleteTechnologyCommand command) {
        validator.ensureExists(command.id());
        repository.deleteById(command.id());
    }

    @Transactional(readOnly = true)
    public TechnologyQueryResultDTO handleGetById(GetTechnologyByIdCommand command) {
        Technology tech = validator.getExisting(command.id());
        return mapToQueryResult(tech);
    }

    @Transactional(readOnly = true)
    public List<TechnologyQueryResultDTO> handleGetAll() {
        return repository.findAll().stream().map(this::mapToQueryResult).toList();
    }

    // --- Mappers ---
    private Technology toDomain(CreateTechnologyCommand c) {
        return new Technology(
                c.name(), c.efficiency(), c.installationCost(),
                c.maintenanceCost(), c.environmentalImpact(),
                c.co2Reduction(), c.energyProduction(), c.energyType());
    }

    private Technology toDomain(UpdateTechnologyCommand c) {
        return new Technology(
                c.name(), c.efficiency(), c.installationCost(),
                c.maintenanceCost(), c.environmentalImpact(),
                c.co2Reduction(), c.energyProduction(), c.energyType());
    }

    private TechnologyQueryResultDTO mapToQueryResult(Technology t) {
        return new TechnologyQueryResultDTO(
                null, t.name(), t.energyType(),
                t.efficiency(), t.installationCost(),
                t.maintenanceCost(), t.environmentalImpact(),
                t.co2Reduction(), t.energyProduction());
    }
}
