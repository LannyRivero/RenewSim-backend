package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.dashboard.application.port.out.PortfolioDashboardQueryPort;
import com.renewsim.backend.simulation_service.delete.application.port.out.DeleteSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.detail.application.port.out.SimulationDetailQueryPort;
import com.renewsim.backend.simulation_service.history.application.port.out.SimulationHistoryQueryPort;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPage;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPageRequest;
import com.renewsim.backend.simulation_service.domain.model.SimulationId;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.purge.application.port.out.PurgeDeletedSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class SimulationRecordRepositoryAdapter
        implements CreateSimulationRepositoryPort, SimulationDetailQueryPort, SimulationHistoryQueryPort,
        DeleteSimulationRepositoryPort, PortfolioDashboardQueryPort, PurgeDeletedSimulationRepositoryPort {

    private final JpaSimulationRepository repository;
    private final SimulationRecordEntityMapper entityMapper;

    @Autowired
    public SimulationRecordRepositoryAdapter(
            JpaSimulationRepository repository,
            ObjectMapper objectMapper) {
        this(repository, new SimulationRecordEntityMapper(
                new SimulationInputSnapshotCodec(objectMapper)));
    }

    SimulationRecordRepositoryAdapter(
            JpaSimulationRepository repository,
            SimulationRecordEntityMapper entityMapper) {
        this.repository = repository;
        this.entityMapper = entityMapper;
    }

    @Override
    public Simulation save(Simulation simulation) {
        SimulationEntity entity = entityMapper.toEntity(simulation);
        SimulationEntity saved = repository.save(entity);
        if (simulation.getId() == null) {
            simulation.assignId(SimulationId.of(saved.getId()));
        }
        return simulation;
    }

    @Override
    public Optional<Simulation> findById(Long id) {
        return repository.findById(id).map(entityMapper::toDomain);
    }

    @Override
    public List<Simulation> findActiveByCreatedBy(String createdBy) {
        return repository.findByCreatedByAndStatusNotOrderByCreatedAtDesc(createdBy, "DELETED")
                .stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public SimulationHistoryPage<SimulationReadModel> searchByCreatedBy(
            String createdBy, String location, SimulationHistoryPageRequest pageRequest) {
        String persistenceSortField = switch (pageRequest.sortField()) {
            case CREATED_AT -> "createdAt";
            case NAME -> "name";
            case TECHNOLOGY -> "energyType";
            case LOCATION -> "location";
            case STATUS -> "status";
        };
        Sort.Direction direction = pageRequest.sortDirection() == SimulationHistoryPageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, persistenceSortField).and(Sort.by(Sort.Direction.DESC, "id"));
        var page = repository.searchHistory(createdBy, location,
                PageRequest.of(pageRequest.page(), pageRequest.size(), sort));
        return new SimulationHistoryPage<>(page.getContent().stream().map(entityMapper::toReadModel).toList(),
                page.getTotalElements(), page.getNumber(), page.getSize(), page.getTotalPages());
    }

    @Override
    public List<SimulationReadModel> findByCreatedByOrderByCreatedAtDesc(String createdBy) {
        return repository.findByCreatedByAndStatusNotOrderByCreatedAtDesc(createdBy, "DELETED")
                .stream()
                .map(entityMapper::toReadModel)
                .toList();
    }

    @Override
    public int purgeDeletedOlderThan(LocalDateTime cutoff) {
        return repository.deleteDeletedOlderThan(cutoff);
    }
}
