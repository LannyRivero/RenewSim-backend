package com.renewsim.backend.simulation_service.history.application;

import com.renewsim.backend.simulation_service.history.application.port.in.ListUserRealSimulationsUseCase;
import com.renewsim.backend.simulation_service.history.application.port.out.SimulationHistoryQueryPort;
import com.renewsim.backend.simulation_service.history.application.result.SimulationHistoryRowResult;
import com.renewsim.backend.simulation_service.history.application.result.UserSimulationListResult;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationResultSnapshotReaderPort;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.renewsim.backend.simulation_service.shared.application.SimulationFormatUtils.formatDate;
import static com.renewsim.backend.simulation_service.shared.application.SimulationNumericUtils.defaultNumber;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListSimulationsService implements ListUserRealSimulationsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListSimulationsService.class);
    private static final String USE_CASE = "history";

    private final SimulationHistoryQueryPort repository;
    private final SimulationResultSnapshotReaderPort snapshotReader;
    private final SimulationUseCaseTelemetry telemetry;

    @Override
    public UserSimulationListResult getUserSimulations(String username) {
        Timer.Sample sample = telemetry.start();
        try {
            List<SimulationHistoryRowResult> items = repository.findByCreatedByOrderByCreatedAtDesc(username)
                    .stream()
                    .map(this::toHistoryRow)
                    .toList();
            UserSimulationListResult result = new UserSimulationListResult(items, items.size());
            telemetry.recordSuccess(USE_CASE, sample);
            log.info("Simulation history retrieved user={} total={}", username, result.total());
            return result;
        } catch (RuntimeException ex) {
            telemetry.recordError(USE_CASE, sample);
            log.warn("Simulation history failed user={} reason={}", username, ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private SimulationHistoryRowResult toHistoryRow(SimulationReadModel simulation) {
        SimulationMetadata metadata = metadataFor(simulation.resultSnapshot());
        return new SimulationHistoryRowResult(
                String.valueOf(simulation.id()),
                simulation.name(),
                simulation.technology(),
                simulation.status() != null ? simulation.status().toLowerCase() : "completed",
                formatDate(simulation.createdAt()),
                simulation.locationLabel(),
                defaultNumber(simulation.annualGenerationKwh()),
                defaultNumber(simulation.annualSavings()),
                defaultNumber(simulation.npv()),
                simulation.irrPct(),
                simulation.recommendation(),
                metadata.modelVersion(),
                metadata.resourceSource());
    }

    private SimulationMetadata metadataFor(String resultSnapshot) {
        try {
            SimulationDetailsResult details = snapshotReader.read(resultSnapshot);
            if (details == null) {
                return SimulationMetadata.empty();
            }
            String resourceSource = details.assumptions() != null && details.assumptions().climateSource() != null
                    ? details.assumptions().climateSource()
                    : details.technical() != null && details.technical().resource() != null
                            ? details.technical().resource().source()
                            : null;
            return new SimulationMetadata(details.modelVersion(), resourceSource);
        } catch (IllegalStateException ex) {
            return SimulationMetadata.empty();
        }
    }

    private record SimulationMetadata(String modelVersion, String resourceSource) {
        private static SimulationMetadata empty() {
            return new SimulationMetadata(null, null);
        }
    }
}
