package com.renewsim.backend.simulation_service.domain.model;

import com.renewsim.backend.shared.domain.exception.InvalidLocationException;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationParameterException;
import com.renewsim.backend.simulation_service.domain.model.vo.Budget;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulationTest {

    @Test
    @DisplayName("assignTechnologies returns a new immutable aggregate instance")
    void assignTechnologiesReturnsNewAggregate() {
        Simulation original = sampleSimulation(List.of(1L));

        Simulation updated = original.assignTechnologies(List.of(2L, 3L));

        assertThat(original).isNotSameAs(updated);
        assertThat(original.technologyIds()).containsExactly(1L);
        assertThat(updated.technologyIds()).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("reconstitute rejects blank createdBy")
    void reconstituteRejectsBlankCreatedBy() {
        assertThatThrownBy(() -> Simulation.reconstitute(
                10L,
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
                "   ",
                LocalDateTime.parse("2026-05-22T12:00:00")))
                .isInstanceOf(InvalidSimulationParameterException.class)
                .hasMessageContaining("owner");
    }

    @Test
    @DisplayName("reconstitute rejects invalid coordinates")
    void reconstituteRejectsInvalidCoordinates() {
        assertThatThrownBy(() -> Simulation.reconstitute(
                10L,
                "Solar Demo",
                "Mendoza",
                150.0,
                -68.8458,
                EnergyType.SOLAR,
                new ProjectSize(100),
                new Budget(90000),
                new EnergyOutput(120000),
                new CO2Reduction(84),
                new ClimateData(10, 3, 1),
                List.of(),
                "alice",
                LocalDateTime.parse("2026-05-22T12:00:00")))
                .isInstanceOf(InvalidLocationException.class);
    }

    private Simulation sampleSimulation(List<Long> technologyIds) {
        return Simulation.reconstitute(
                10L,
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
                technologyIds,
                "alice",
                LocalDateTime.parse("2026-05-22T12:00:00"));
    }
}
