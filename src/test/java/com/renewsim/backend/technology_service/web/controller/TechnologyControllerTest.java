package com.renewsim.backend.technology_service.web.controller;

import com.renewsim.backend.technology_service.application.command.CreateTechnologyCommand;
import com.renewsim.backend.technology_service.application.command.DeleteTechnologyCommand;
import com.renewsim.backend.technology_service.application.command.UpdateTechnologyCommand;
import com.renewsim.backend.technology_service.application.port.in.CreateTechnologyUseCase;
import com.renewsim.backend.technology_service.application.port.in.DeleteTechnologyUseCase;
import com.renewsim.backend.technology_service.application.port.in.EstimateTechnologyUseCase;
import com.renewsim.backend.technology_service.application.port.in.GetTechnologyUseCase;
import com.renewsim.backend.technology_service.application.port.in.UpdateTechnologyUseCase;
import com.renewsim.backend.technology_service.web.dto.TechnologyRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TechnologyControllerTest {

    @Mock
    private CreateTechnologyUseCase createUseCase;
    @Mock
    private UpdateTechnologyUseCase updateUseCase;
    @Mock
    private DeleteTechnologyUseCase deleteUseCase;
    @Mock
    private GetTechnologyUseCase getUseCase;
    @Mock
    private EstimateTechnologyUseCase estimateUseCase;

    @InjectMocks
    private TechnologyController controller;

    @Test
    @DisplayName("create should map request DTO to create command")
    void createShouldMapRequestDtoToCreateCommand() {
        TechnologyRequestDTO request = new TechnologyRequestDTO(
                "Solar Panel", 0.85, 1200, 100, 10, 250, 18, "SOLAR");

        controller.create(request);

        ArgumentCaptor<CreateTechnologyCommand> captor = ArgumentCaptor.forClass(CreateTechnologyCommand.class);
        verify(createUseCase).createTechnology(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Solar Panel");
        assertThat(captor.getValue().energyType()).isEqualTo("SOLAR");
    }

    @Test
    @DisplayName("update should map request DTO and path id to update command")
    void updateShouldMapRequestDtoAndPathIdToUpdateCommand() {
        TechnologyRequestDTO request = new TechnologyRequestDTO(
                "Wind Turbine", 0.35, 2000, 120, 8, 300, 35, "WIND");

        controller.update(7L, request);

        ArgumentCaptor<UpdateTechnologyCommand> captor = ArgumentCaptor.forClass(UpdateTechnologyCommand.class);
        verify(updateUseCase).updateTechnology(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(7L);
        assertThat(captor.getValue().name()).isEqualTo("Wind Turbine");
    }

    @Test
    @DisplayName("delete should return HTTP 204")
    void deleteShouldReturnHttp204() {
        var response = controller.delete(9L);

        verify(deleteUseCase).deleteTechnology(new DeleteTechnologyCommand(9L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }
}
