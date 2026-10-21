package com.renewsim.backend.simulation_service.history.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.history.application.port.out.SimulationHistoryQueryPort;
import com.renewsim.backend.simulation_service.history.application.result.UserSimulationListResult;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPage;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPageRequest;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationResultSnapshotReaderPort;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationResultSnapshotJacksonReader;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.renewsim.backend.simulation_service.support.SimulationDetailTestFixtures.completedSimulation;
import static com.renewsim.backend.simulation_service.support.SimulationDetailTestFixtures.minimalResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ListSimulationsServiceTest {

        @Mock
        private SimulationHistoryQueryPort repository;

        private final SimulationResultSnapshotReaderPort snapshotReader = new SimulationResultSnapshotJacksonReader(
                        new ObjectMapper().findAndRegisterModules());

        private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        @Test
        @DisplayName("location suggestions normalize query and delegate to the user-scoped repository")
        void locationSuggestionsNormalizeQueryAndDelegateToUserScopedRepository() {
                ListSimulationsService service = new ListSimulationsService(repository,
                                snapshotReader,
                                new SimulationUseCaseTelemetry(meterRegistry));
                when(repository.findLocationSuggestionsByCreatedBy("alice", "malaga", 5))
                                .thenReturn(List.of("Málaga, ES"));

                var suggestions = service.getUserLocationSuggestions("alice", " MÁLAGA ", 5);

                assertThat(suggestions).containsExactly("Málaga, ES");
                verify(repository).findLocationSuggestionsByCreatedBy("alice", "malaga", 5);
        }

        @Test
        @DisplayName("getUserSimulations maps stored summary columns for scenario-created simulations")
        void getUserSimulationsMapsStoredSummaryColumnsForScenarioCreatedSimulations() {
                ListSimulationsService service = new ListSimulationsService(repository,
                                snapshotReader,
                                new SimulationUseCaseTelemetry(meterRegistry));
                String snapshot;
                try {
                        var baseline = minimalResult();
                        var custom = new com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult(
                                        baseline.id(),
                                        baseline.status(),
                                        baseline.createdAt(),
                                        baseline.updatedAt(),
                                        "solar-interview-v2",
                                        baseline.technology(),
                                        baseline.location(),
                                        baseline.summary(),
                                        baseline.input(),
                                        baseline.technical(),
                                        baseline.financial(),
                                        new com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult.Assumptions(
                                                        baseline.assumptions().discountRatePct(),
                                                        baseline.assumptions().projectLifetimeYears(),
                                                        baseline.assumptions().degradationRateAnnualPct(),
                                                        baseline.assumptions().electricityPurchasePricePerKwh(),
                                                        baseline.assumptions().exportPricePerKwh(),
                                                        "INTERVIEW_SOURCE",
                                                        baseline.assumptions().climatePeriod()),
                                        baseline.warnings());
                        snapshot = new ObjectMapper().findAndRegisterModules().writeValueAsString(custom);
                } catch (Exception ex) {
                        throw new RuntimeException(ex);
                }
                var simulation = completedSimulation(55L, "alice", snapshot);
                simulation.assignScenarioId(42L);
                simulation.assignTechnologyIds(List.of(11L, 12L));
                var readModel = new SimulationReadModel(
                                simulation.getId().value(),
                                simulation.getName(),
                                simulation.getTechnology().value(),
                                simulation.getStatus().name(),
                                simulation.getLocation().label(),
                                simulation.getAnnualGenerationKwh(),
                                simulation.getAnnualSavings(),
                                simulation.getNpv(),
                                simulation.getIrrPct(),
                                simulation.getRecommendation(),
                                simulation.getEconomics().capexTotal(),
                                simulation.getResultSnapshot(),
                                simulation.getCreatedAt());
                var pageRequest = new SimulationHistoryPageRequest(0, 20,
                                SimulationHistoryPageRequest.SortField.CREATED_AT,
                                SimulationHistoryPageRequest.SortDirection.DESC);
                when(repository.searchByCreatedBy("alice", "Solar", "malaga centro", pageRequest))
                                .thenReturn(new SimulationHistoryPage<>(List.of(readModel), 1, 0, 20, 1));

                UserSimulationListResult response = service.getUserSimulations("alice", " Solar ", "  MÁLAGA   Centro ",
                                pageRequest);

                assertThat(response.total()).isEqualTo(1);
                assertThat(response.items().getFirst().annualSavings()).isEqualTo(68700.0);
                assertThat(response.items().getFirst().technology()).isEqualTo("solar");
                assertThat(response.items().getFirst().modelVersion()).isEqualTo("solar-interview-v2");
                assertThat(response.items().getFirst().resourceSource()).isEqualTo("INTERVIEW_SOURCE");
                assertThat(response.page()).isZero();
                assertThat(response.size()).isEqualTo(20);
        }
}
