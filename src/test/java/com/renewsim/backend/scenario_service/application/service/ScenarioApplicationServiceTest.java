package com.renewsim.backend.scenario_service.application.service;

import com.renewsim.backend.scenario_service.application.command.CreateScenarioCommand;
import com.renewsim.backend.scenario_service.application.command.GetScenarioByIdCommand;
import com.renewsim.backend.scenario_service.application.command.UpdateScenarioCommand;
import com.renewsim.backend.scenario_service.application.mapper.ScenarioDtoMapper;
import com.renewsim.backend.scenario_service.application.port.out.ScenarioRepositoryPort;
import com.renewsim.backend.scenario_service.application.result.ScenarioResponseDTO;
import com.renewsim.backend.scenario_service.domain.exception.ScenarioNotFoundException;
import com.renewsim.backend.scenario_service.domain.model.Scenario;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultCapacityKw;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultConsumption;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultTariff;
import com.renewsim.backend.scenario_service.domain.model.vo.ScenarioTechnologyId;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import com.renewsim.backend.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioApplicationServiceTest {

    @Mock
    private ScenarioRepositoryPort repository;

    @Mock
    private ScenarioDtoMapper dtoMapper;

    @Mock
    private ScenarioValidator validator;

    @InjectMocks
    private ScenarioApplicationService service;

    @Test
    @DisplayName("getAllActiveScenarios should return mapped active scenarios")
    void getAllActiveScenariosShouldReturnMappedActiveScenarios() {
        Scenario scenario = new Scenario(
                1L, "Residential Solar", null,
                new ScenarioTechnologyId(5L), new DefaultCapacityKw(5.0),
                new Money(new BigDecimal("7500.00"), "USD"), new DefaultTariff(0.15), new DefaultConsumption(6000.0),
                new ClimateData(5.5, 3.2, 22.0), true);
        when(repository.findAllActive()).thenReturn(List.of(scenario));
        when(dtoMapper.toResponse(scenario)).thenReturn(new ScenarioResponseDTO(
                1L, "Residential Solar", null, 5L, 5.0,
                new Money(new BigDecimal("7500.00"), "USD"), 0.15, 6000.0,
                new ClimateData(5.5, 3.2, 22.0), true));

        var result = service.getAllActiveScenarios();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Residential Solar");
    }

    @Test
    @DisplayName("getScenarioById should throw when scenario is missing or inactive")
    void getScenarioByIdShouldThrowWhenScenarioIsMissingOrInactive() {
        when(validator.getExistingActiveScenario(9L)).thenThrow(new ScenarioNotFoundException(9L));

        assertThrows(ScenarioNotFoundException.class, () -> service.getScenarioById(new GetScenarioByIdCommand(9L)));
    }

    @Test
    @DisplayName("createScenario should persist a new active scenario")
    void createScenarioShouldPersistANewActiveScenario() {
        CreateScenarioCommand command = new CreateScenarioCommand(
                "Residential Solar", null,
                new ScenarioTechnologyId(5L), new DefaultCapacityKw(5.0),
                new Money(new BigDecimal("7500.00"), "USD"), new DefaultTariff(0.15), new DefaultConsumption(6000.0),
                new ClimateData(5.5, 3.2, 22.0));
        Scenario saved = new Scenario(
                1L, "Residential Solar", null,
                new ScenarioTechnologyId(5L), new DefaultCapacityKw(5.0),
                new Money(new BigDecimal("7500.00"), "USD"), new DefaultTariff(0.15), new DefaultConsumption(6000.0),
                new ClimateData(5.5, 3.2, 22.0), true);
        org.mockito.Mockito.doNothing().when(validator).ensureActiveTechnologyExists(5L);
        when(repository.save(any(Scenario.class))).thenReturn(saved);
        when(dtoMapper.toResponse(saved)).thenReturn(new ScenarioResponseDTO(
                1L, "Residential Solar", null, 5L, 5.0,
                new Money(new BigDecimal("7500.00"), "USD"), 0.15, 6000.0,
                new ClimateData(5.5, 3.2, 22.0), true));

        var result = service.createScenario(command);

        assertThat(result.id()).isEqualTo(1L);
        verify(repository).save(any(Scenario.class));
    }

    @Test
    @DisplayName("updateScenario should preserve active flag")
    void updateScenarioShouldPreserveActiveFlag() {
        Scenario existing = new Scenario(
                1L, "Residential Solar", null,
                new ScenarioTechnologyId(5L), new DefaultCapacityKw(5.0),
                new Money(new BigDecimal("7500.00"), "USD"), new DefaultTariff(0.15), new DefaultConsumption(6000.0),
                new ClimateData(5.5, 3.2, 22.0), true);
        UpdateScenarioCommand command = new UpdateScenarioCommand(
                1L, "Updated Scenario", "desc",
                new ScenarioTechnologyId(6L), new DefaultCapacityKw(6.0),
                new Money(new BigDecimal("8000.00"), "USD"), new DefaultTariff(0.20), new DefaultConsumption(6500.0),
                new ClimateData(6.0, 4.0, 21.0));
        when(validator.getExistingActiveScenario(1L)).thenReturn(existing);
        org.mockito.Mockito.doNothing().when(validator).ensureActiveTechnologyExists(6L);
        when(repository.save(any(Scenario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dtoMapper.toResponse(any(Scenario.class))).thenAnswer(invocation -> {
            Scenario scenario = invocation.getArgument(0);
            return new ScenarioResponseDTO(
                    scenario.getId(),
                    scenario.getName(),
                    scenario.getDescription(),
                    scenario.getTechnologyId(),
                    scenario.getDefaultCapacityKw(),
                    scenario.getDefaultInvestment(),
                    scenario.getDefaultTariff(),
                    scenario.getDefaultConsumption(),
                    scenario.getClimateProfile(),
                    scenario.isActive());
        });

        var result = service.updateScenario(command);

        assertThat(result.name()).isEqualTo("Updated Scenario");
        assertThat(result.isActive()).isTrue();
    }
}
