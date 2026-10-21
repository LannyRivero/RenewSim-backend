package com.renewsim.backend.simulation_service.dashboard.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.dashboard.application.port.out.PortfolioDashboardQueryPort;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordEntityMapper;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PortfolioDashboardQueryAdapter implements PortfolioDashboardQueryPort {

    private final JpaSimulationRepository repository;
    private final SimulationRecordEntityMapper entityMapper;

    @Override
    public List<SimulationReadModel> findByCreatedByOrderByCreatedAtDesc(String createdBy) {
        return repository.findByCreatedByAndStatusNotOrderByCreatedAtDesc(createdBy, "DELETED")
                .stream()
                .map(entityMapper::toReadModel)
                .toList();
    }
}
