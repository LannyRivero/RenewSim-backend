package com.renewsim.backend.simulation_service.application.service;

import com.renewsim.backend.simulation_service.application.command.CreateSimulationCommand;
import com.renewsim.backend.simulation_service.application.command.GetSimulationByIdCommand;
import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.application.port.out.SimulationRepositoryPort;
import com.renewsim.backend.simulation_service.application.result.SimulationDetailResultDTO;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.Budget;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectSize;
import com.renewsim.backend.technology_service.application.service.TechnologyRecommenderService;
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
    private TechnologyRecommenderService recommender;

    @Test
    @DisplayName("createSimulation fetches climate from provider when command climate is null")
    void createSimulationFetchesClimateFromProviderWhenCommandClimateIsNull() {
        SimulationApplicationService service = new SimulationApplicationService(
                repository,
                validator,
                calculator,
                climateProvider,
                recommender);

        CreateSimulationCommand command = new CreateSimulationCommand(
                "Mendoza",
                EnergyType.SOLAR,
                100,
                10000,
                null,
                List.of(),
                "alice");

        ClimateData providedByPort = new ClimateData(12, 6, 2);
        doNothing().when(validator).validateProjectSize(100);
        doNothing().when(validator).validateBudget(10000);
        when(climateProvider.fetchClimateData("Mendoza")).thenReturn(providedByPort);
        when(calculator.calculateEnergyOutput(any(Simulation.class))).thenReturn(new EnergyOutput(120000));
        when(calculator.calculateCo2Reduction(any(EnergyOutput.class))).thenReturn(new CO2Reduction(84));

        Simulation afterCreateSave = new Simulation(
                99L,
                "Mendoza",
                EnergyType.SOLAR,
                new ProjectSize(100),
                new Budget(10000),
                new EnergyOutput(120000),
                new CO2Reduction(84),
                providedByPort,
                List.of(),
                "alice",
                LocalDateTime.parse("2026-05-22T09:00:00"));

        Simulation afterRecommendationSave = new Simulation(
                99L,
                "Mendoza",
                EnergyType.SOLAR,
                new ProjectSize(100),
                new Budget(10000),
                new EnergyOutput(120000),
                new CO2Reduction(84),
                providedByPort,
                List.of(1L),
                "alice",
                LocalDateTime.parse("2026-05-22T09:00:00"));

        when(repository.save(any(Simulation.class))).thenReturn(afterCreateSave, afterRecommendationSave);
        when(recommender.recommendFor(afterCreateSave)).thenReturn(List.of(1L));

        service.createSimulation(command);

        verify(climateProvider).fetchClimateData("Mendoza");

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

        ClimateData climateData = new ClimateData(5, 7, 1);
        CreateSimulationCommand command = new CreateSimulationCommand(
                "Mendoza",
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
                "Mendoza",
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
                "Mendoza",
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

        ClimateData climateData = new ClimateData(5, 7, 1);
        CreateSimulationCommand command = new CreateSimulationCommand(
                "Mendoza",
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
                "Mendoza",
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
        verify(climateProvider, never()).fetchClimateData(any(String.class));

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
                "Cordoba",
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
        when(calculator.calculateSavings(simulation)).thenReturn(5000.0);
        when(calculator.calculateRoiYears(simulation)).thenReturn(4.0);

        SimulationDetailResultDTO detail = service.getSimulationById(new GetSimulationByIdCommand(77L, "alice", false));

        assertThat(detail.technologyIds()).containsExactly(2L, 3L);
    }
}
