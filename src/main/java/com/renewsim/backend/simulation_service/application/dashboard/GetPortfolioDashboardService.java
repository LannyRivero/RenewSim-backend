package com.renewsim.backend.simulation_service.application.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.application.port.out.SimulationRecordRepositoryPort;
import com.renewsim.backend.simulation_service.application.port.out.TechnologyLookupPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Application service that orchestrates dashboard retrieval and ranking.
 */
@Service
@Transactional(readOnly = true)
public class GetPortfolioDashboardService implements GetPortfolioDashboardUseCase {

        private final SimulationRecordRepositoryPort repository;
        private final ScenarioSnapshotAssembler snapshotAssembler;
        private final PortfolioDashboardAggregator dashboardAggregator;

        public GetPortfolioDashboardService(
                        SimulationRecordRepositoryPort repository,
                        TechnologyLookupPort technologyLookupPort,
                        ObjectMapper objectMapper) {
                this(repository, new ScenarioSnapshotAssembler(
                                technologyLookupPort,
                                objectMapper,
                                new PortfolioScenarioScoringPolicy()),
                                new PortfolioDashboardAggregator());
        }

        GetPortfolioDashboardService(
                        SimulationRecordRepositoryPort repository,
                        ScenarioSnapshotAssembler snapshotAssembler,
                        PortfolioDashboardAggregator dashboardAggregator) {
                this.repository = repository;
                this.snapshotAssembler = snapshotAssembler;
                this.dashboardAggregator = dashboardAggregator;
        }

        @Override
        public PortfolioDashboardResult getDashboard(String username) {
                List<ScenarioSnapshot> snapshots = repository.findByCreatedByOrderByCreatedAtDesc(username).stream()
                                .map(snapshotAssembler::toSnapshot)
                                .toList();

                List<ScenarioSnapshot> ranked = snapshots.stream()
                                .sorted(Comparator.comparingInt(ScenarioSnapshot::score).reversed()
                                                .thenComparing(ScenarioSnapshot::createdAt,
                                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .toList();

                return dashboardAggregator.buildDashboard(snapshots, ranked);
        }
}
