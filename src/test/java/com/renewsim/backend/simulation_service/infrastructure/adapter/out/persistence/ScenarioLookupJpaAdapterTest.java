package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.scenario_service.application.port.in.ScenarioCatalogLookupUseCase;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioLookupJpaAdapterTest {

    @Mock
    private ScenarioCatalogLookupUseCase scenarioCatalogLookupUseCase;

    @Test
    @DisplayName("findActiveScenarioById maps the active scenario snapshot")
    void findActiveScenarioByIdMapsTheActiveScenarioSnapshot() {
        when(scenarioCatalogLookupUseCase.findActiveScenarioById(7L))
                .thenReturn(Optional.of(new ScenarioCatalogLookupUseCase.ScenarioCatalogSnapshot(
                        7L,
                        "Hogar solar - Sevilla",
                        1L,
                        5.0,
                        12000.0,
                        "EUR",
                        0.15,
                        6000.0)));

        ScenarioLookupJpaAdapter adapter = new ScenarioLookupJpaAdapter(scenarioCatalogLookupUseCase);

        Optional<ScenarioLookupPort.ScenarioSnapshot> result = adapter.findActiveScenarioById(7L);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow()).isEqualTo(new ScenarioLookupPort.ScenarioSnapshot(
                7L,
                "Hogar solar - Sevilla",
                1L,
                5.0,
                12000.0,
                "EUR",
                0.15,
                6000.0));
    }

    @Test
    @DisplayName("findActiveScenarioById returns empty when the scenario does not exist or is inactive")
    void findActiveScenarioByIdReturnsEmptyWhenTheScenarioDoesNotExistOrIsInactive() {
        when(scenarioCatalogLookupUseCase.findActiveScenarioById(7L)).thenReturn(Optional.empty());

        ScenarioLookupJpaAdapter adapter = new ScenarioLookupJpaAdapter(scenarioCatalogLookupUseCase);

        assertThat(adapter.findActiveScenarioById(7L)).isEmpty();
    }
}
