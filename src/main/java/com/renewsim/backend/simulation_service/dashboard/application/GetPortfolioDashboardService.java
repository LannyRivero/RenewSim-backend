package com.renewsim.backend.simulation_service.dashboard.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.dashboard.application.port.out.PortfolioDashboardQueryPort;
import com.renewsim.backend.simulation_service.dashboard.application.port.in.GetPortfolioDashboardUseCase;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardResult;
import com.renewsim.backend.simulation_service.dashboard.application.projection.ScenarioSnapshot;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import org.springframework.beans.factory.annotation.Autowired;
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

        private final PortfolioDashboardQueryPort repository;
        private final ScenarioSnapshotAssembler snapshotAssembler;
        private final PortfolioDashboardAggregator dashboardAggregator;

        @Autowired
        public GetPortfolioDashboardService(
                        PortfolioDashboardQueryPort repository,
                        TechnologyLookupPort technologyLookupPort,
                        ObjectMapper objectMapper) {
                this(repository, new ScenarioSnapshotAssembler(
                                technologyLookupPort,
                                objectMapper,
                                new PortfolioScenarioScoringPolicy()),
                                new PortfolioDashboardAggregator());
        }

        GetPortfolioDashboardService(
                        PortfolioDashboardQueryPort repository,
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
