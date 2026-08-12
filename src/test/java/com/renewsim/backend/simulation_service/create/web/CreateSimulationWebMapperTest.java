package com.renewsim.backend.simulation_service.create.web;

import com.renewsim.backend.simulation_service.create.web.dto.CreateSimulationRequestDTO;
import com.renewsim.backend.simulation_service.create.web.dto.SimulationLocationRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSimulationWebMapperTest {

    private final CreateSimulationWebMapper mapper = new CreateSimulationWebMapper();

    @Test
    @DisplayName("toCommand maps request DTO into application command")
    void toCommandMapsRequestDtoIntoApplicationCommand() {
        CreateSimulationRequestDTO request = request();

        var command = mapper.toCommand(request, "alice");

        assertThat(command.name()).isEqualTo("Solar - Sevilla");
        assertThat(command.technology().value()).isEqualTo("solar");
        assertThat(command.location().country()).isEqualTo("Spain");
        assertThat(command.system().installedCapacityKw()).isEqualTo(300.0);
        assertThat(command.demand().annualConsumptionKwh()).isEqualTo(120000.0);
        assertThat(command.economics().currency().value()).isEqualTo("EUR");
        assertThat(command.technologyIds()).containsExactly(11L, 12L);
        assertThat(command.createdBy()).isEqualTo("alice");
    }

    private CreateSimulationRequestDTO request() {
        return new CreateSimulationRequestDTO(
                "Solar - Sevilla",
                "solar",
                new SimulationLocationRequestDTO("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", "ES"),
                new CreateSimulationRequestDTO.SystemDTO(300, 0.81, 0.5, 99,
                        new CreateSimulationRequestDTO.LossesPctDTO(2, 6, 1, 3, 1)),
                new CreateSimulationRequestDTO.DemandDTO(120000,
                        List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                10000d)),
                new CreateSimulationRequestDTO.EconomicsDTO("EUR", 315000, 7200, 0.18, 0.07, 8, 20),
                List.of(11L, 12L));
    }
}
