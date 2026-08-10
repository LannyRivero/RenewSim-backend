package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.scenario_service.application.command.GetScenarioByIdCommand;
import com.renewsim.backend.scenario_service.application.port.in.GetScenarioUseCase;
import com.renewsim.backend.scenario_service.application.result.ScenarioResponseDTO;
import com.renewsim.backend.scenario_service.domain.exception.ScenarioNotFoundException;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import com.renewsim.backend.shared.domain.vo.Money;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioLookupJpaAdapterTest {

    @Mock
    private GetScenarioUseCase getScenarioUseCase;

    @InjectMocks
    private ScenarioLookupJpaAdapter adapter;

    @Test
    @DisplayName("findActiveScenarioById maps the active scenario snapshot")
    void findActiveScenarioByIdMapsTheActiveScenarioSnapshot() {
        when(getScenarioUseCase.getScenarioById(any(GetScenarioByIdCommand.class))).thenReturn(new ScenarioResponseDTO(
                7L,
                "Hogar solar - Sevilla",
                "Scenario description",
                1L,
                5.0,
                new Money(new BigDecimal("12000.00"), "EUR"),
                0.15,
                6000.0,
                new ClimateData(5.4, 3.1, 18.0),
                true));

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
        when(getScenarioUseCase.getScenarioById(any(GetScenarioByIdCommand.class)))
                .thenThrow(new ScenarioNotFoundException(7L));

        assertThat(adapter.findActiveScenarioById(7L)).isEmpty();
    }
}
