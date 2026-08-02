package com.renewsim.backend.simulation_service.create.web;

import com.renewsim.backend.simulation_service.web.dto.CreateSolarSimulationRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSimulationWebMapperTest {

    private final CreateSimulationWebMapper mapper = new CreateSimulationWebMapper();

    @Test
    @DisplayName("toCommand maps request DTO into application command")
    void toCommandMapsRequestDtoIntoApplicationCommand() {
        CreateSolarSimulationRequestDTO request = request();

        var command = mapper.toCommand(request, "alice");

        assertThat(command.name()).isEqualTo("Solar - Sevilla");
        assertThat(command.technology().value()).isEqualTo("solar");
        assertThat(command.location().country()).isEqualTo("Spain");
        assertThat(command.system().installedCapacityKw()).isEqualTo(300.0);
        assertThat(command.demand().annualConsumptionKwh()).isEqualTo(120000.0);
        assertThat(command.economics().currency().value()).isEqualTo("EUR");
        assertThat(command.createdBy()).isEqualTo("alice");
    }

    private CreateSolarSimulationRequestDTO request() {
        return new CreateSolarSimulationRequestDTO(
                "Solar - Sevilla",
                new CreateSolarSimulationRequestDTO.LocationDTO("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", "ES"),
                new CreateSolarSimulationRequestDTO.SolarSystemDTO(300, 0.81, 0.5, 99,
                        new CreateSolarSimulationRequestDTO.LossesPctDTO(2, 6, 1, 3, 1)),
                new CreateSolarSimulationRequestDTO.DemandDTO(120000,
                        List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                10000d)),
                new CreateSolarSimulationRequestDTO.EconomicsDTO("EUR", 315000, 7200, 0.18, 0.07, 8, 20));
    }
}
