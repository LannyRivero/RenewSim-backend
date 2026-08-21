package com.renewsim.backend.simulation_service.dashboard.application;

import com.renewsim.backend.simulation_service.dashboard.application.port.in.GetPortfolioDashboardUseCase;
import com.renewsim.backend.simulation_service.dashboard.application.port.out.PortfolioDashboardQueryPort;
import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardResult;
import com.renewsim.backend.simulation_service.dashboard.application.projection.ScenarioSnapshot;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Application service that orchestrates dashboard retrieval and ranking.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPortfolioDashboardService implements GetPortfolioDashboardUseCase {

        private static final Logger log = LoggerFactory.getLogger(GetPortfolioDashboardService.class);
        private static final String USE_CASE = "dashboard";

        private final PortfolioDashboardQueryPort repository;
        private final ScenarioSnapshotAssembler snapshotAssembler;
        private final PortfolioDashboardAggregator dashboardAggregator;
        private final SimulationUseCaseTelemetry telemetry;

        @Override
        public PortfolioDashboardResult getDashboard(String username) {
                Timer.Sample sample = telemetry.start();
                try {
                        List<SimulationReadModel> simulations = repository
                                        .findByCreatedByOrderByCreatedAtDesc(username);
                        List<ScenarioSnapshot> snapshots = simulations.stream()
                                        .map(snapshotAssembler::toSnapshot)
                                        .toList();

                        List<ScenarioSnapshot> ranked = snapshots.stream()
                                        .sorted(Comparator.comparingInt(ScenarioSnapshot::score).reversed()
                                                        .thenComparing(ScenarioSnapshot::createdAt,
                                                                        Comparator.nullsLast(
                                                                                        Comparator.reverseOrder())))
                                        .toList();

                        PortfolioDashboardResult result = dashboardAggregator.buildDashboard(snapshots, ranked);
                        telemetry.recordSuccess(USE_CASE, sample);
                        log.info("Simulation dashboard built user={} totalSimulations={} prioritized={}",
                                        username,
                                        result.summary().totalSimulations(),
                                        result.prioritizedScenarios().size());
                        return result;
                } catch (RuntimeException ex) {
                        telemetry.recordError(USE_CASE, sample);
                        log.warn("Simulation dashboard failed user={} reason={}", username,
                                        ex.getClass().getSimpleName());
                        throw ex;
                }
        }
}
