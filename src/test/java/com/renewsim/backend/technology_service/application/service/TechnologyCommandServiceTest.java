package com.renewsim.backend.technology_service.application.service;

import com.renewsim.backend.technology_service.application.command.*;
import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.application.result.*;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.application.mapper.TechnologyDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TechnologyCommandService}.
 * Ensures correct orchestration between Validator, Repository, and Mapper
 * layers.
 */
@ExtendWith(MockitoExtension.class)
class TechnologyCommandServiceTest {

    @Mock
    private TechnologyRepositoryPort repository;

    @Mock
    private TechnologyValidator validator;

    @Mock
    private TechnologyDtoMapper dtoMapper;

    @InjectMocks
    private TechnologyCommandService service;

    private Technology domain;
    private CreateTechnologyCommand createCommand;
    private UpdateTechnologyCommand updateCommand;
    private DeleteTechnologyCommand deleteCommand;
    private GetTechnologyByIdCommand getByIdCommand;

    @BeforeEach
    void setup() {
        domain = new Technology(
                "Solar Panel",
                EnergyType.SOLAR,
                new Efficiency(0.85),
                new InstallationCost(BigDecimal.valueOf(1200)),
                new MaintenanceCost(BigDecimal.valueOf(100)),
                new EnvironmentalImpact(10.0),
                new Co2Reduction(BigDecimal.valueOf(250)),
                new EnergyProduction(5000));

        createCommand = new CreateTechnologyCommand(
                "Solar Panel", 0.85, 1200, 100, 10, 250, 5000, "SOLAR");

        updateCommand = new UpdateTechnologyCommand(
                1L, "Solar Panel", 0.90, 1400, 120, 8, 300, 6000, "SOLAR");

        deleteCommand = new DeleteTechnologyCommand(1L);
        getByIdCommand = new GetTechnologyByIdCommand(1L);
    }

    // ============================================================
    // CREATE
    // ============================================================
    @Test
    @DisplayName("Should create a new technology successfully")
    void shouldCreateTechnology() {
        TechnologyCreationResultDTO expectedDTO = new TechnologyCreationResultDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 100, 10, 250, 5000, true, "Created successfully");

        when(repository.save(any(Technology.class))).thenReturn(domain);
        when(dtoMapper.toCreationResult(domain)).thenReturn(expectedDTO);

        var result = service.handleCreate(createCommand);

        assertNotNull(result);
        assertEquals("Solar Panel", result.name());
        verify(validator, times(1)).ensureUniqueName("Solar Panel");
        verify(repository, times(1)).save(any(Technology.class));
        verify(dtoMapper, times(1)).toCreationResult(domain);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @Test
    @DisplayName("Should update an existing technology successfully")
    void shouldUpdateTechnology() {

        Technology existing = new Technology(
                1L,
                "Solar Panel",
                EnergyType.SOLAR,
                new Efficiency(0.85),
                new InstallationCost(BigDecimal.valueOf(1200)),
                new MaintenanceCost(BigDecimal.valueOf(100)),
                new EnvironmentalImpact(10.0),
                new Co2Reduction(BigDecimal.valueOf(250)),
                new EnergyProduction(5000));

        TechnologyUpdateResultDTO expectedDTO = new TechnologyUpdateResultDTO(
                1L, "Solar Panel", "SOLAR", 0.90, 1400, 120, 8, 300, 6000, true, "Updated successfully");

        when(validator.getExisting(1L)).thenReturn(existing);
        when(repository.save(any(Technology.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dtoMapper.toUpdateResult(any(Technology.class))).thenReturn(expectedDTO);

        var result = service.handleUpdate(updateCommand);

        assertNotNull(result);
        assertEquals("Solar Panel", result.name());
        assertEquals(0.90, result.efficiency());
        assertEquals(1400, result.installationCost());
        verify(validator, times(1)).getExisting(1L);
        verify(repository, times(1)).save(any(Technology.class));
        verify(dtoMapper, times(1)).toUpdateResult(any(Technology.class));
    }

    // ============================================================
    // DELETE
    // ============================================================
    @Test
    @DisplayName("Should delete an existing technology successfully")
    void shouldDeleteTechnology() {
        doNothing().when(validator).ensureExists(1L);
        doNothing().when(repository).deleteById(1L);

        service.handleDelete(deleteCommand);

        verify(validator, times(1)).ensureExists(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    // ============================================================
    // GET BY ID
    // ============================================================
    @Test
    @DisplayName("Should get a technology by ID successfully")
    void shouldGetTechnologyById() {
        TechnologyQueryResultDTO expectedDTO = new TechnologyQueryResultDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 100, 10, 250, 5000);

        when(validator.getExisting(1L)).thenReturn(domain);
        when(dtoMapper.toQueryResult(domain)).thenReturn(expectedDTO);

        var result = service.handleGetById(getByIdCommand);

        assertNotNull(result);
        assertEquals("Solar Panel", result.name());
        verify(validator, times(1)).getExisting(1L);
        verify(dtoMapper, times(1)).toQueryResult(domain);
    }

    // ============================================================
    // GET ALL
    // ============================================================
    @Test
    @DisplayName("Should get all technologies successfully")
    void shouldGetAllTechnologies() {
        List<Technology> domains = List.of(domain);
        List<TechnologyQueryResultDTO> dtos = List.of(
                new TechnologyQueryResultDTO(1L, "Solar Panel", "SOLAR", 0.85, 1200, 100, 10, 250, 5000));

        when(repository.findAll()).thenReturn(domains);
        when(dtoMapper.toQueryResult(domain)).thenReturn(dtos.get(0));

        var result = service.handleGetAll();

        assertEquals(1, result.size());
        verify(repository, times(1)).findAll();
        verify(dtoMapper, times(1)).toQueryResult(domain);
    }
}
