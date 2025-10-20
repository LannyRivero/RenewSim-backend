package com.renewsim.backend.technology_service.application.service;

import com.renewsim.backend.technology_service.application.command.*;
import com.renewsim.backend.technology_service.application.result.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ✅ Unit tests for {@link TechnologyApplicationService}.
 * Ensures all methods delegate correctly to {@link TechnologyCommandService}.
 */
@ExtendWith(MockitoExtension.class)
class TechnologyApplicationServiceTest {

    @Mock
    private TechnologyCommandService commandService;

    @InjectMocks
    private TechnologyApplicationService applicationService;

    private CreateTechnologyCommand createCommand;
    private UpdateTechnologyCommand updateCommand;
    private DeleteTechnologyCommand deleteCommand;
    private GetTechnologyByIdCommand getByIdCommand;

    @BeforeEach
    void setup() {
        createCommand = new CreateTechnologyCommand(
                "Solar Panel", 0.85, 1200, 100, 10, 250, 5000, "SOLAR");

        updateCommand = new UpdateTechnologyCommand(
                1L, "Updated Solar Panel", 0.90, 1500, 120, 8, 280, 6000, "SOLAR");

        deleteCommand = new DeleteTechnologyCommand(1L);
        getByIdCommand = new GetTechnologyByIdCommand(1L);
    }

    // ------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------
    @Test
    @DisplayName("Should delegate createTechnology() to commandService.handleCreate()")
    void shouldDelegateCreateTechnology() {
        var expected = new TechnologyCreationResultDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 100, 10, 250, 5000, true, "Created successfully");

        when(commandService.handleCreate(createCommand)).thenReturn(expected);

        var result = applicationService.createTechnology(createCommand);

        assertNotNull(result);
        assertEquals("Solar Panel", result.name());
        assertEquals("SOLAR", result.energyType());
        verify(commandService, times(1)).handleCreate(createCommand);
    }

    // ------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------
    @Test
    @DisplayName("Should delegate updateTechnology() to commandService.handleUpdate()")
    void shouldDelegateUpdateTechnology() {
        var expected = new TechnologyUpdateResultDTO(
                1L, "Updated Solar Panel", "SOLAR", 0.90, 1500, 120, 8, 280, 6000, true, "Updated successfully");

        when(commandService.handleUpdate(updateCommand)).thenReturn(expected);

        var result = applicationService.updateTechnology(updateCommand);

        assertNotNull(result);
        assertEquals("Updated Solar Panel", result.name());
        assertTrue(result.success());
        verify(commandService, times(1)).handleUpdate(updateCommand);
    }

    // ------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------
    @Test
    @DisplayName("Should delegate deleteTechnology() to commandService.handleDelete()")
    void shouldDelegateDeleteTechnology() {
        doNothing().when(commandService).handleDelete(deleteCommand);

        applicationService.deleteTechnology(deleteCommand);

        verify(commandService, times(1)).handleDelete(deleteCommand);
    }

    // ------------------------------------------------------------
    // GET BY ID
    // ------------------------------------------------------------
    @Test
    @DisplayName("Should delegate getTechnologyById() to commandService.handleGetById()")
    void shouldDelegateGetTechnologyById() {
        var expected = new TechnologyQueryResultDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 100, 10, 250, 5000);

        when(commandService.handleGetById(getByIdCommand)).thenReturn(expected);

        var result = applicationService.getTechnologyById(getByIdCommand);

        assertNotNull(result);
        assertEquals("Solar Panel", result.name());
        assertEquals("SOLAR", result.energyType());
        verify(commandService, times(1)).handleGetById(getByIdCommand);
    }

    // ------------------------------------------------------------
    // GET ALL
    // ------------------------------------------------------------
    @Test
    @DisplayName("Should delegate getAllTechnologies() to commandService.handleGetAll()")
    void shouldDelegateGetAllTechnologies() {
        var dtoList = List.of(
                new TechnologyQueryResultDTO(1L, "Solar", "SOLAR", 0.85, 1200, 100, 10, 250, 5000),
                new TechnologyQueryResultDTO(2L, "Wind", "EOLIC", 0.70, 2000, 150, 8, 300, 7000));

        when(commandService.handleGetAll()).thenReturn(dtoList);

        var result = applicationService.getAllTechnologies();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(commandService, times(1)).handleGetAll();
    }
}
