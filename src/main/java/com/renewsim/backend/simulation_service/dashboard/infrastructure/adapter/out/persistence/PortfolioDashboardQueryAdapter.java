package com.renewsim.backend.simulation_service.dashboard.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.dashboard.application.port.out.PortfolioDashboardQueryPort;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordEntityMapper;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PortfolioDashboardQueryAdapter implements PortfolioDashboardQueryPort {

    private final JpaSimulationRepository repository;
    private final SimulationRecordEntityMapper entityMapper;

    /**
     * Loads dashboard read models while the persistence context is open.
     *
     * <p>
     * Mapping resolves the current result revision through a lazy association; the
     * read-only
     * transaction keeps that association available without allowing dashboard reads
     * to write.
     * </p>
     *
     * @param createdBy owner whose non-deleted simulations are requested
     * @return the owner's simulations ordered by creation time, newest first
     */
    @Override
    @Transactional(readOnly = true)
    public List<SimulationReadModel> findByCreatedByOrderByCreatedAtDesc(String createdBy) {
        return repository.findByCreatedByAndStatusNotOrderByCreatedAtDesc(createdBy, "DELETED")
                .stream()
                .map(entityMapper::toReadModel)
                .toList();
    }
}
