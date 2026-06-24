package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.renewsim.backend.shared.exception.ConflictException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.Budget;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectSize;
import com.renewsim.backend.simulation_service.infrastructure.mapper.SimulationMapper;
import com.renewsim.backend.simulation_service.infrastructure.persistence.entity.SimulationEntity;

@ExtendWith(MockitoExtension.class)
class SimulationRepositoryAdapterTest {

    @Mock
    private JpaSimulationRepository repository;
    @Mock
    private SimulationMapper mapper;

    @Test
    @DisplayName("save translates duplicate simulation constraint into ConflictException")
    void saveDuplicateSimulation() {
        SimulationRepositoryAdapter adapter = new SimulationRepositoryAdapter(repository, mapper);
        Simulation simulation = Simulation.reconstitute(
                null,
                "Solar Demo",
                "Mendoza",
                -32.8895,
                -68.8458,
                EnergyType.SOLAR,
                new ProjectSize(100),
                new Budget(90000),
                new EnergyOutput(120000),
                new CO2Reduction(84),
                new ClimateData(10, 3, 1),
                List.of(),
                "alice",
                LocalDateTime.parse("2026-05-22T12:00:00"));

        when(mapper.toEntity(any(Simulation.class))).thenReturn(new SimulationEntity());
        when(repository.save(any(SimulationEntity.class))).thenThrow(
                new DataIntegrityViolationException("Duplicate entry", new RuntimeException("uk_simulations_owner_name_energy_location")));

        assertThatThrownBy(() -> adapter.save(simulation))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Simulation already exists");
    }
}
