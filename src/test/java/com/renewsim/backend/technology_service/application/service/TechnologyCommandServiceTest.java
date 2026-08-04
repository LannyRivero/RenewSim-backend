package com.renewsim.backend.technology_service.application.service;

import com.renewsim.backend.technology_service.application.command.CreateTechnologyCommand;
import com.renewsim.backend.technology_service.application.command.DeleteTechnologyCommand;
import com.renewsim.backend.technology_service.application.command.GetTechnologyByIdCommand;
import com.renewsim.backend.technology_service.application.command.UpdateTechnologyCommand;
import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.application.result.TechnologyCreationResultDTO;
import com.renewsim.backend.technology_service.application.result.TechnologyResponseDTO;
import com.renewsim.backend.technology_service.application.result.TechnologyUpdateResultDTO;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.renewsim.backend.shared.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.Instant;
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
                new CapacityFactor(18.0));

        createCommand = new CreateTechnologyCommand(
                "Solar Panel", 0.85, 1200, 100, 10, 250, 18.0, "SOLAR");

        updateCommand = new UpdateTechnologyCommand(
                1L, "Solar Panel", 0.90, 1400, 120, 8, 300, 35.0, "SOLAR");

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
                30,
                new MaintenanceCost(BigDecimal.valueOf(100)),
                "Existing description",
                false,
                Instant.parse("2026-05-01T10:15:30Z"),
                Instant.parse("2026-05-02T11:15:30Z"),
                new EnvironmentalImpact(10.0),
                new Co2Reduction(BigDecimal.valueOf(250)),
                new CapacityFactor(18.0));

        TechnologyUpdateResultDTO expectedDTO = new TechnologyUpdateResultDTO(
                1L, "Solar Panel", "SOLAR", 0.90, 1400, 120, 8, 300, 6000, true, "Updated successfully");

        when(validator.getExistingActive(1L)).thenReturn(existing);
        when(repository.save(any(Technology.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dtoMapper.toUpdateResult(any(Technology.class))).thenReturn(expectedDTO);

        var result = service.handleUpdate(updateCommand);

        assertNotNull(result);
        assertEquals("Solar Panel", result.name());
        assertEquals(0.90, result.efficiency());
        assertEquals(1400, result.installationCost());
        var technologyCaptor = org.mockito.ArgumentCaptor.forClass(Technology.class);
        verify(validator, times(1)).getExistingActive(1L);
        verify(repository, times(1)).save(technologyCaptor.capture());
        verify(dtoMapper, times(1)).toUpdateResult(any(Technology.class));

        var savedTechnology = technologyCaptor.getValue();
        assertEquals(30, savedTechnology.getLifespanYears());
        assertEquals("Existing description", savedTechnology.getDescription());
        assertFalse(savedTechnology.isActive());
        assertEquals(Instant.parse("2026-05-01T10:15:30Z"), savedTechnology.getCreatedAt());
    }

    // ============================================================
    // DELETE
    // ============================================================
    @Test
    @DisplayName("Should delete an existing technology successfully")
    void shouldDeleteTechnology() {
        var existing = new Technology(
                1L,
                "Solar Panel",
                EnergyType.SOLAR,
                new Efficiency(0.85),
                new InstallationCost(BigDecimal.valueOf(1200)),
                25,
                new MaintenanceCost(BigDecimal.valueOf(100)),
                "Existing description",
                true,
                Instant.parse("2026-05-01T10:15:30Z"),
                Instant.parse("2026-05-02T11:15:30Z"),
                new EnvironmentalImpact(10.0),
                new Co2Reduction(BigDecimal.valueOf(250)),
                new CapacityFactor(18.0));

        when(validator.getExistingActive(1L)).thenReturn(existing);
        when(repository.save(any(Technology.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handleDelete(deleteCommand);

        var technologyCaptor = org.mockito.ArgumentCaptor.forClass(Technology.class);
        verify(validator, times(1)).getExistingActive(1L);
        verify(repository, times(1)).save(technologyCaptor.capture());
        assertFalse(technologyCaptor.getValue().isActive());
    }

    // ============================================================
    // GET BY ID
    // ============================================================
    @Test
    @DisplayName("Should get a technology by ID successfully")
    void shouldGetTechnologyById() {
        TechnologyResponseDTO expectedDTO = new TechnologyResponseDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 25, 100,
                null, true, null, null, 10, 250, 18);

        when(validator.getExisting(1L)).thenReturn(domain);
        when(dtoMapper.toResponse(domain)).thenReturn(expectedDTO);

        var result = service.handleGetById(getByIdCommand);

        assertNotNull(result);
        assertEquals("Solar Panel", result.name());
        verify(validator, times(1)).getExisting(1L);
        verify(dtoMapper, times(1)).toResponse(domain);
    }

    @Test
    @DisplayName("Should get an inactive technology by ID for historical reads")
    void shouldGetInactiveTechnologyByIdForHistoricalReads() {
        Technology inactiveTechnology = new Technology(
                1L,
                "Solar Panel",
                EnergyType.SOLAR,
                new Efficiency(0.85),
                new InstallationCost(BigDecimal.valueOf(1200)),
                25,
                new MaintenanceCost(BigDecimal.valueOf(100)),
                "Retired technology",
                false,
                Instant.parse("2026-05-01T10:15:30Z"),
                Instant.parse("2026-05-02T11:15:30Z"),
                new EnvironmentalImpact(10.0),
                new Co2Reduction(BigDecimal.valueOf(250)),
                new CapacityFactor(18.0));
        TechnologyResponseDTO response = new TechnologyResponseDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 25, 100,
                "Retired technology", false, Instant.parse("2026-05-01T10:15:30Z"),
                Instant.parse("2026-05-02T11:15:30Z"), 10, 250, 18);

        when(validator.getExisting(1L)).thenReturn(inactiveTechnology);
        when(dtoMapper.toResponse(inactiveTechnology)).thenReturn(response);

        var result = service.handleGetById(getByIdCommand);

        assertNotNull(result);
        assertFalse(result.isActive());
        verify(validator, times(1)).getExisting(1L);
        verify(dtoMapper, times(1)).toResponse(inactiveTechnology);
    }

    // ============================================================
    // GET ALL
    // ============================================================
    @Test
    @DisplayName("Should get all technologies successfully")
    void shouldGetAllTechnologies() {
        var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));
        var page = new PageImpl<>(List.of(domain), pageable, 1);
        var dto = new TechnologyResponseDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 25, 100,
                null, true, null, null, 10, 250, 18);

        when(repository.findAllActive(pageable)).thenReturn(page);
        when(dtoMapper.toResponse(domain)).thenReturn(dto);

        var result = service.handleGetAll(0, 20, null, null, null, "asc");

        assertEquals(1, result.getContent().size());
        verify(repository, times(1)).findAllActive(pageable);
        verify(dtoMapper, times(1)).toResponse(domain);
    }

    @Test
    @DisplayName("Should sort technologies by energy type")
    void shouldSortTechnologiesByEnergyType() {
        var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "energyType"));
        var page = new PageImpl<>(List.of(domain), pageable, 1);
        var dto = new TechnologyResponseDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 25, 100,
                null, true, null, null, 10, 250, 18);

        when(repository.findAllActive(pageable)).thenReturn(page);
        when(dtoMapper.toResponse(domain)).thenReturn(dto);

        var result = service.handleGetAll(0, 20, null, null, "energyType", "desc");

        assertEquals(1, result.getContent().size());
        verify(repository, times(1)).findAllActive(pageable);
    }

    @Test
    @DisplayName("Should sort technologies by CO2 reduction")
    void shouldSortTechnologiesByCo2Reduction() {
        var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "co2ReductionFactor"));
        var page = new PageImpl<>(List.of(domain), pageable, 1);
        var dto = new TechnologyResponseDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 25, 100,
                null, true, null, null, 10, 250, 18);

        when(repository.findAllActive(pageable)).thenReturn(page);
        when(dtoMapper.toResponse(domain)).thenReturn(dto);

        var result = service.handleGetAll(0, 20, null, null, "co2Reduction", "asc");

        assertEquals(1, result.getContent().size());
        verify(repository, times(1)).findAllActive(pageable);
    }

    @Test
    @DisplayName("Should search active technologies by name")
    void shouldSearchActiveTechnologiesByName() {
        var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));
        var page = new PageImpl<>(List.of(domain), pageable, 1);
        var dto = new TechnologyResponseDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 25, 100,
                null, true, null, null, 10, 250, 18);

        when(repository.findAllActiveByNameContaining("sol", pageable)).thenReturn(page);
        when(dtoMapper.toResponse(domain)).thenReturn(dto);

        var result = service.handleGetAll(0, 20, null, " sol ", null, "asc");

        assertEquals(1, result.getContent().size());
        verify(repository, times(1)).findAllActiveByNameContaining("sol", pageable);
    }

    @Test
    @DisplayName("Should search active technologies by energy type and name")
    void shouldSearchActiveTechnologiesByEnergyTypeAndName() {
        var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));
        var page = new PageImpl<>(List.of(domain), pageable, 1);
        var dto = new TechnologyResponseDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 25, 100,
                null, true, null, null, 10, 250, 18);

        when(repository.findActiveByEnergyTypeAndNameContaining(EnergyType.SOLAR, "sol", pageable)).thenReturn(page);
        when(dtoMapper.toResponse(domain)).thenReturn(dto);

        var result = service.handleGetAll(0, 20, "SOLAR", "sol", null, "asc");

        assertEquals(1, result.getContent().size());
        verify(repository, times(1)).findActiveByEnergyTypeAndNameContaining(EnergyType.SOLAR, "sol", pageable);
    }

    @Test
    @DisplayName("Should ignore short search terms")
    void shouldIgnoreShortSearchTerms() {
        var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));
        var page = new PageImpl<>(List.of(domain), pageable, 1);
        var dto = new TechnologyResponseDTO(
                1L, "Solar Panel", "SOLAR", 0.85, 1200, 25, 100,
                null, true, null, null, 10, 250, 18);

        when(repository.findAllActive(pageable)).thenReturn(page);
        when(dtoMapper.toResponse(domain)).thenReturn(dto);

        var result = service.handleGetAll(0, 20, null, "ab", null, "asc");

        assertEquals(1, result.getContent().size());
        verify(repository, times(1)).findAllActive(pageable);
        verify(repository, never()).findAllActiveByNameContaining(anyString(), any());
    }

    @Test
    @DisplayName("Should reject unsupported sort field")
    void shouldRejectUnsupportedSortField() {
        var exception = assertThrows(BadRequestException.class,
                () -> service.handleGetAll(0, 20, null, null, "unsupported", "asc"));

        assertEquals("Invalid sortBy value. Allowed values: name, energyType, efficiency, co2Reduction",
                exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should reject unsupported sort direction")
    void shouldRejectUnsupportedSortDirection() {
        var exception = assertThrows(BadRequestException.class,
                () -> service.handleGetAll(0, 20, null, null, "name", "down"));

        assertEquals("Invalid sortDirection value. Allowed values: asc, desc", exception.getMessage());
        verifyNoInteractions(repository);
    }
}
