package com.renewsim.backend.scenario_service.web.controller;

import com.renewsim.backend.scenario_service.application.command.CreateScenarioCommand;
import com.renewsim.backend.scenario_service.application.command.UpdateScenarioCommand;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultCapacityKw;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultTariff;
import com.renewsim.backend.scenario_service.domain.model.vo.ScenarioTechnologyId;
import com.renewsim.backend.scenario_service.application.port.in.CreateScenarioUseCase;
import com.renewsim.backend.scenario_service.application.port.in.GetScenarioUseCase;
import com.renewsim.backend.scenario_service.application.port.in.UpdateScenarioUseCase;
import com.renewsim.backend.scenario_service.web.dto.ScenarioRequestDTO;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScenarioControllerTest {

    @Mock
    private GetScenarioUseCase getScenarioUseCase;
    @Mock
    private CreateScenarioUseCase createScenarioUseCase;
    @Mock
    private UpdateScenarioUseCase updateScenarioUseCase;

    @InjectMocks
    private ScenarioController controller;

    @Test
    @DisplayName("create should map request DTO to create command")
    void createShouldMapRequestDtoToCreateCommand() {
        ScenarioRequestDTO request = new ScenarioRequestDTO(
                "Residential Solar", null, 5L, 5.0,
                new BigDecimal("7500.00"), "USD", 0.15, 6000.0,
                new ClimateData(5.5, 3.2, 22.0));

        controller.create(request);

        ArgumentCaptor<CreateScenarioCommand> captor = ArgumentCaptor.forClass(CreateScenarioCommand.class);
        verify(createScenarioUseCase).createScenario(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Residential Solar");
        assertThat(captor.getValue().technologyId()).isEqualTo(new ScenarioTechnologyId(5L));
        assertThat(captor.getValue().defaultCapacityKw()).isEqualTo(new DefaultCapacityKw(5.0));
        assertThat(captor.getValue().defaultInvestment().amount()).isEqualByComparingTo("7500.00");
    }

    @Test
    @DisplayName("update should map request DTO and path id to update command")
    void updateShouldMapRequestDtoAndPathIdToUpdateCommand() {
        ScenarioRequestDTO request = new ScenarioRequestDTO(
                "Updated Scenario", "desc", 6L, 6.0,
                new BigDecimal("8000.00"), "USD", 0.20, 6500.0,
                new ClimateData(6.0, 4.0, 21.0));

        controller.update(7L, request);

        ArgumentCaptor<UpdateScenarioCommand> captor = ArgumentCaptor.forClass(UpdateScenarioCommand.class);
        verify(updateScenarioUseCase).updateScenario(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(7L);
        assertThat(captor.getValue().defaultTariff()).isEqualTo(new DefaultTariff(0.20));
    }

    @Test
    @DisplayName("create should return HTTP 201")
    void createShouldReturnHttp201() {
        ScenarioRequestDTO request = new ScenarioRequestDTO(
                "Residential Solar", null, 5L, 5.0,
                new BigDecimal("7500.00"), "USD", 0.15, 6000.0,
                new ClimateData(5.5, 3.2, 22.0));

        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
