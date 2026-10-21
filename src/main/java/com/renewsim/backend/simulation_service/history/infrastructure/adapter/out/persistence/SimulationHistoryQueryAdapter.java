package com.renewsim.backend.simulation_service.history.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.history.application.port.out.SimulationHistoryQueryPort;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPage;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPageRequest;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordEntityMapper;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SimulationHistoryQueryAdapter implements SimulationHistoryQueryPort {

        private final JpaSimulationRepository repository;
        private final SimulationRecordEntityMapper entityMapper;

        @Override
        public SimulationHistoryPage<SimulationReadModel> searchByCreatedBy(
                        String createdBy, String name, String location, SimulationHistoryPageRequest pageRequest) {
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
                var page = repository.searchHistory(createdBy, name, location,
                                PageRequest.of(pageRequest.page(), pageRequest.size(), sort));
                return new SimulationHistoryPage<>(page.getContent().stream().map(entityMapper::toReadModel).toList(),
                                page.getTotalElements(), page.getNumber(), page.getSize(), page.getTotalPages());
        }

        @Override
        public List<String> findLocationSuggestionsByCreatedBy(String createdBy, String normalizedQuery, int limit) {
                return repository.findLocationSuggestionsByCreatedBy(
                                createdBy, normalizedQuery, PageRequest.of(0, limit));
        }
}
