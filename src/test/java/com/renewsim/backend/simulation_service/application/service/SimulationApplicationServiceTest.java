package com.renewsim.backend.simulation_service.application.service;

import com.renewsim.backend.simulation_service.application.command.CreateSimulationCommand;
import com.renewsim.backend.simulation_service.application.command.GetSimulationByIdCommand;
import com.renewsim.backend.simulation_service.application.command.UpdateSimulationCommand;
import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.application.port.out.SimulationRepositoryPort;
import com.renewsim.backend.simulation_service.application.port.out.TechnologyRecommendationPort;
import com.renewsim.backend.simulation_service.application.result.SimulationDetailResultDTO;
import com.renewsim.backend.simulation_service.application.result.SimulationUpdateResultDTO;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.Budget;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationApplicationServiceTest {

    @Mock
    private SimulationRepositoryPort repository;
    @Mock
    private SimulationValidator validator;
    @Mock
    private SimulationCalculator calculator;
    @Mock
    private ClimateDataProviderPort climateProvider;
    @Mock
    private TechnologyRecommendationPort recommender;

    @Test
    @DisplayName("createSimulation fetches climate from provider when command climate is null")
    void createSimulationFetchesClimateFromProviderWhenCommandClimateIsNull() {
        SimulationApplicationService service = new SimulationApplicationService(
                repository,
                validator,
                calculator,
                climateProvider,
                recommender);
        when(repository.findDuplicate("alice", "Test Solar", "SOLAR", -32.8895, -68.8458)).thenReturn(Optional.empty());

        CreateSimulationCommand command = new CreateSimulationCommand(
                "Test Solar",
                "Mendoza",
                -32.8895,
                -68.8458,
                EnergyType.SOLAR,
                100,
                0,
                null,
                List.of(),
                "alice");

        ClimateData providedByPort = new ClimateData(12, 6, 2);
        doNothing().when(validator).validateProjectSize(100);
        when(calculator.estimateCapex(EnergyType.SOLAR, 100)).thenReturn(90000.0);
        doNothing().when(validator).validateBudget(90000);
        when(climateProvider.fetchClimateData(-32.8895, -68.8458)).thenReturn(providedByPort);
        when(calculator.calculateEnergyOutput(any(Simulation.class))).thenReturn(new EnergyOutput(120000));
        when(calculator.calculateCo2Reduction(any(EnergyOutput.class))).thenReturn(new CO2Reduction(84));

        Simulation afterCreateSave = new Simulation(
                99L,
                "Test Solar",
                "Mendoza",
                -32.8895,
                -68.8458,
                EnergyType.SOLAR,
                new ProjectSize(100),
                new Budget(90000),
                new EnergyOutput(120000),
                new CO2Reduction(84),
                providedByPort,
                List.of(),
                "alice",
                LocalDateTime.parse("2026-05-22T09:00:00"));

        Simulation afterRecommendationSave = new Simulation(
                99L,
                "Test Solar",
                "Mendoza",
                -32.8895,
                -68.8458,
                EnergyType.SOLAR,
                new ProjectSize(100),
                new Budget(90000),
                new EnergyOutput(120000),
                new CO2Reduction(84),
                providedByPort,
                List.of(1L),
                "alice",
                LocalDateTime.parse("2026-05-22T09:00:00"));

        when(repository.save(any(Simulation.class))).thenReturn(afterCreateSave, afterRecommendationSave);
        when(recommender.recommendFor(afterCreateSave)).thenReturn(List.of(1L));

        service.createSimulation(command);

        verify(climateProvider).fetchClimateData(-32.8895, -68.8458);

        ArgumentCaptor<Simulation> saveCaptor = ArgumentCaptor.forClass(Simulation.class);
        verify(repository, times(2)).save(saveCaptor.capture());
        assertThat(saveCaptor.getAllValues().get(0).climateData()).isEqualTo(providedByPort);
    }

    @Test
    @DisplayName("RED->GREEN: createSimulation assigns recommendations when command has no technologies")
    void createSimulationAssignsRecommendationsWhenCommandHasNoTechnologies() {
        SimulationApplicationService service = new SimulationApplicationService(
                repository,
                validator,
                calculator,
                climateProvider,
                recommender);
        when(repository.findDuplicate("alice", "Test Solar", "SOLAR", -32.8895, -68.8458)).thenReturn(Optional.empty());

        ClimateData climateData = new ClimateData(5, 7, 1);
        CreateSimulationCommand command = new CreateSimulationCommand(
                "Test Solar",
                "Mendoza",
                -32.8895,
                -68.8458,
                EnergyType.SOLAR,
                100,
                10000,
                climateData,
                List.of(),
                "alice");

        doNothing().when(validator).validateProjectSize(100);
        doNothing().when(validator).validateBudget(10000);
        when(calculator.calculateEnergyOutput(any(Simulation.class))).thenReturn(new EnergyOutput(120000));
        when(calculator.calculateCo2Reduction(any(EnergyOutput.class))).thenReturn(new CO2Reduction(84));

        Simulation afterCreateSave = new Simulation(
                99L,
                "Test Solar",
                "Mendoza",
                -32.8895,
                -68.8458,
                EnergyType.SOLAR,
                new ProjectSize(100),
                new Budget(10000),
                new EnergyOutput(120000),
                new CO2Reduction(84),
                climateData,
                List.of(),
                "alice",
                LocalDateTime.parse("2026-05-22T09:00:00"));

        Simulation afterRecommendationSave = new Simulation(
                99L,
                "Test Solar",
                "Mendoza",
                -32.8895,
                -68.8458,
                EnergyType.SOLAR,
                new ProjectSize(100),
                new Budget(10000),
                new EnergyOutput(120000),
                new CO2Reduction(84),
                climateData,
                List.of(1L),
                "alice",
                LocalDateTime.parse("2026-05-22T09:00:00"));

        when(repository.save(any(Simulation.class))).thenReturn(afterCreateSave, afterRecommendationSave);
        when(recommender.recommendFor(afterCreateSave)).thenReturn(List.of(1L));

        service.createSimulation(command);

        verify(repository, times(2)).save(any(Simulation.class));
        ArgumentCaptor<Simulation> saveCaptor = ArgumentCaptor.forClass(Simulation.class);
        verify(repository, times(2)).save(saveCaptor.capture());
        assertThat(saveCaptor.getAllValues().get(1).technologyIds()).containsExactly(1L);
    }

    @Test
    @DisplayName("TRIANGULATE: createSimulation keeps explicit command technologies and skips recommender")
    void createSimulationKeepsExplicitCommandTechnologies() {
        SimulationApplicationService service = new SimulationApplicationService(
                repository,
                validator,
                calculator,
                climateProvider,
                recommender);
        when(repository.findDuplicate("alice", "Test Solar", "SOLAR", -32.8895, -68.8458)).thenReturn(Optional.empty());

        ClimateData climateData = new ClimateData(5, 7, 1);
        CreateSimulationCommand command = new CreateSimulationCommand(
                "Test Solar",
                "Mendoza",
                -32.8895,
                -68.8458,
                EnergyType.SOLAR,
                100,
                10000,
                climateData,
                List.of(9L, 10L),
                "alice");

        doNothing().when(validator).validateProjectSize(100);
        doNothing().when(validator).validateBudget(10000);
        when(calculator.calculateEnergyOutput(any(Simulation.class))).thenReturn(new EnergyOutput(120000));
        when(calculator.calculateCo2Reduction(any(EnergyOutput.class))).thenReturn(new CO2Reduction(84));

        Simulation saved = new Simulation(
                100L,
                "Test Solar",
                "Mendoza",
                -32.8895,
                -68.8458,
                EnergyType.SOLAR,
                new ProjectSize(100),
                new Budget(10000),
                new EnergyOutput(120000),
                new CO2Reduction(84),
                climateData,
                List.of(),
                "alice",
                LocalDateTime.parse("2026-05-22T09:00:00"));

        when(repository.save(any(Simulation.class))).thenReturn(saved, saved);

        service.createSimulation(command);

        verify(repository, times(2)).save(any(Simulation.class));
        verify(recommender, never()).recommendFor(any(Simulation.class));
        verify(climateProvider, never()).fetchClimateData(any(Double.class), any(Double.class));

        ArgumentCaptor<Simulation> saveCaptor = ArgumentCaptor.forClass(Simulation.class);
        verify(repository, times(2)).save(saveCaptor.capture());
        assertThat(saveCaptor.getAllValues().get(1).technologyIds()).containsExactly(9L, 10L);
    }

    @Test
    @DisplayName("RED->GREEN: getSimulationById includes technologyIds in detail result")
    void getSimulationByIdIncludesTechnologyIdsInDetailResult() {
        SimulationApplicationService service = new SimulationApplicationService(
                repository,
                validator,
                calculator,
                climateProvider,
                recommender);

        Simulation simulation = new Simulation(
                77L,
                "Wind Demo",
                "Cordoba",
                -31.4167,
                -64.1833,
                EnergyType.WIND,
                new ProjectSize(80),
                new Budget(20000),
                new EnergyOutput(80000),
                new CO2Reduction(40),
                new ClimateData(4, 9, 0),
                List.of(2L, 3L),
                "alice",
                LocalDateTime.parse("2026-05-22T12:00:00"));

        when(repository.findById(77L)).thenReturn(Optional.of(simulation));
        SimulationDetailResultDTO detail = service.getSimulationById(new GetSimulationByIdCommand(77L, "alice", false));

        assertThat(detail.technologyIds()).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("updateSimulation fetches climate when command has null and existing has no persisted climate")
    void updateSimulationFetchesClimateWhenCommandAndExistingClimateAreMissing() {
        SimulationApplicationService service = new SimulationApplicationService(
                repository,
                validator,
                calculator,
                climateProvider,
                recommender);

        Simulation existing = new Simulation(
                10L,
                "Solar Rosario",
                "Rosario",
                -32.9442,
                -60.6505,
                EnergyType.SOLAR,
                new ProjectSize(50),
                new Budget(5000),
                new EnergyOutput(50000),
                new CO2Reduction(20),
                null,
                List.of(),
                "alice",
                LocalDateTime.parse("2026-05-22T12:00:00"));

        when(repository.findById(10L)).thenReturn(Optional.of(existing));

        ClimateData providedClimate = new ClimateData(10, 3, 1);
        when(climateProvider.fetchClimateData(-32.9442, -60.6505)).thenReturn(providedClimate);
        when(calculator.calculateEnergyOutput(any(Simulation.class))).thenReturn(new EnergyOutput(60000));
        when(calculator.calculateCo2Reduction(any(EnergyOutput.class))).thenReturn(new CO2Reduction(30));
        when(repository.save(any(Simulation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSimulationCommand command = new UpdateSimulationCommand(
                10L,
                "Solar Rosario",
                "Rosario",
                -32.9442,
                -60.6505,
                EnergyType.SOLAR,
                55,
                5200,
                null,
                List.of(),
                "alice");

        SimulationUpdateResultDTO result = service.updateSimulation(command);

        assertThat(result.id()).isEqualTo(10L);
        verify(climateProvider).fetchClimateData(-32.9442, -60.6505);
    }

    @Test
    @DisplayName("updateSimulation preserves existing technologyIds when command does not send them")
    void updateSimulationPreservesExistingTechnologyIdsWhenMissingInCommand() {
        SimulationApplicationService service = new SimulationApplicationService(
                repository,
                validator,
                calculator,
                climateProvider,
                recommender);

        ClimateData existingClimate = new ClimateData(10, 3, 1);
        Simulation existing = new Simulation(
                10L,
                "Solar Rosario",
                "Rosario",
                -32.9442,
                -60.6505,
                EnergyType.SOLAR,
                new ProjectSize(50),
                new Budget(5000),
                new EnergyOutput(50000),
                new CO2Reduction(20),
                existingClimate,
                List.of(7L, 8L),
                "alice",
                LocalDateTime.parse("2026-05-22T12:00:00"));

        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(calculator.calculateEnergyOutput(any(Simulation.class))).thenReturn(new EnergyOutput(60000));
        when(calculator.calculateCo2Reduction(any(EnergyOutput.class))).thenReturn(new CO2Reduction(30));
        when(repository.save(any(Simulation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSimulationCommand command = new UpdateSimulationCommand(
                10L,
                "Solar Rosario",
                "Rosario",
                -32.9442,
                -60.6505,
                EnergyType.SOLAR,
                55,
                5200,
                null,
                List.of(),
                "alice");

        service.updateSimulation(command);

        ArgumentCaptor<Simulation> saveCaptor = ArgumentCaptor.forClass(Simulation.class);
        verify(repository).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue().technologyIds()).containsExactly(7L, 8L);
        verify(climateProvider, never()).fetchClimateData(any(Double.class), any(Double.class));
    }

    @Test
    @DisplayName("updateSimulation refreshes climate when coordinates change even if existing climate is populated")
    void updateSimulationRefreshesClimateWhenCoordinatesChange() {
        SimulationApplicationService service = new SimulationApplicationService(
                repository,
                validator,
                calculator,
                climateProvider,
                recommender);

        ClimateData existingClimate = new ClimateData(10, 3, 1);
        Simulation existing = new Simulation(
                10L,
                "Solar Rosario",
                "Rosario",
                -32.9442,
                -60.6505,
                EnergyType.SOLAR,
                new ProjectSize(50),
                new Budget(5000),
                new EnergyOutput(50000),
                new CO2Reduction(20),
                existingClimate,
                List.of(7L, 8L),
                "alice",
                LocalDateTime.parse("2026-05-22T12:00:00"));

        ClimateData refreshedClimate = new ClimateData(12, 4, 2);

        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(climateProvider.fetchClimateData(-34.6037, -58.3816)).thenReturn(refreshedClimate);
        when(calculator.calculateEnergyOutput(any(Simulation.class))).thenReturn(new EnergyOutput(60000));
        when(calculator.calculateCo2Reduction(any(EnergyOutput.class))).thenReturn(new CO2Reduction(30));
        when(repository.save(any(Simulation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSimulationCommand command = new UpdateSimulationCommand(
                10L,
                "Solar Buenos Aires",
                "Buenos Aires",
                -34.6037,
                -58.3816,
                EnergyType.SOLAR,
                55,
                5200,
                null,
                List.of(),
                "alice");

        service.updateSimulation(command);

        ArgumentCaptor<Simulation> saveCaptor = ArgumentCaptor.forClass(Simulation.class);
        verify(repository).save(saveCaptor.capture());
        verify(climateProvider).fetchClimateData(-34.6037, -58.3816);
        assertThat(saveCaptor.getValue().climateData()).isEqualTo(refreshedClimate);
    }

}
