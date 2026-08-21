package com.renewsim.backend.simulation_service.detail.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.shared.exception.ForbiddenException;
import com.renewsim.backend.simulation_service.detail.application.port.out.SimulationDetailQueryPort;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.renewsim.backend.simulation_service.support.SimulationDetailTestFixtures.completedSimulation;
import static com.renewsim.backend.simulation_service.support.SimulationDetailTestFixtures.minimalResult;
import static com.renewsim.backend.simulation_service.support.SimulationDetailTestFixtures.snapshotReader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSimulationServiceTest {

        @Mock
        private SimulationDetailQueryPort repository;

        private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        @Test
        @DisplayName("getSimulationById enforces ownership for non admins")
        void getSimulationByIdEnforcesOwnership() throws Exception {
                GetSimulationService service = new GetSimulationService(repository, snapshotReader(),
                                new SimulationUseCaseTelemetry(meterRegistry));
                SimulationDetailsResult result = minimalResult();
                var simulation = completedSimulation(55L, "alice",
                                new ObjectMapper().findAndRegisterModules().writeValueAsString(result));
                when(repository.findById(55L)).thenReturn(Optional.of(simulation));

                assertThatThrownBy(() -> service.getSimulationById(55L, "bob", false))
                                .isInstanceOf(ForbiddenException.class);
                assertThat(service.getSimulationById(55L, "alice", false).id()).isEqualTo("55");
        }

        @Test
        @DisplayName("getSimulationById fails when snapshot is missing")
        void getSimulationByIdFailsWhenSnapshotMissing() {
                GetSimulationService service = new GetSimulationService(repository, snapshotReader(),
                                new SimulationUseCaseTelemetry(meterRegistry));
                var simulation = completedSimulation(55L, "alice", null);
                when(repository.findById(55L)).thenReturn(Optional.of(simulation));

                assertThatThrownBy(() -> service.getSimulationById(55L, "alice", false))
                                .isInstanceOf(SimulationNotFoundException.class);
        }

        @Test
        @DisplayName("getSimulationById degrades invalid snapshots to not found")
        void getSimulationByIdDegradesInvalidSnapshotsToNotFound() {
                GetSimulationService service = new GetSimulationService(repository, snapshotReader(),
                                new SimulationUseCaseTelemetry(meterRegistry));
                var simulation = completedSimulation(55L, "alice", "{ bad json");
                when(repository.findById(55L)).thenReturn(Optional.of(simulation));

                assertThatThrownBy(() -> service.getSimulationById(55L, "alice", false))
                                .isInstanceOf(SimulationNotFoundException.class);
                assertThat(meterRegistry.counter("simulation_service_use_case_total", "use_case", "detail", "outcome",
                                "degraded").count())
                                .isEqualTo(1.0d);
                assertThat(meterRegistry.counter("simulation_service_snapshot_degraded_total", "reason",
                                "invalid_result_snapshot").count())
                                .isEqualTo(1.0d);
        }

        @Test
        @DisplayName("getSimulationById degrades null-decoded snapshots to not found")
        void getSimulationByIdDegradesNullDecodedSnapshotsToNotFound() {
                GetSimulationService service = new GetSimulationService(repository, snapshotReader(),
                                new SimulationUseCaseTelemetry(meterRegistry));
                var simulation = completedSimulation(55L, "alice", "null");
                when(repository.findById(55L)).thenReturn(Optional.of(simulation));

                assertThatThrownBy(() -> service.getSimulationById(55L, "alice", false))
                                .isInstanceOf(SimulationNotFoundException.class);
                assertThat(meterRegistry.counter("simulation_service_use_case_total", "use_case", "detail", "outcome",
                                "degraded").count())
                                .isGreaterThanOrEqualTo(1.0d);
                assertThat(meterRegistry.counter("simulation_service_snapshot_degraded_total", "reason",
                                "invalid_result_snapshot").count())
                                .isGreaterThanOrEqualTo(1.0d);
        }
}
