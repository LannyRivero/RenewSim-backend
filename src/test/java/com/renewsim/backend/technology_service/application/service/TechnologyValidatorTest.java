package com.renewsim.backend.technology_service.application.service;

import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.domain.exception.TechnologyNotFoundException;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ✅ Unit tests for {@link TechnologyValidator}.
 * Ensures repository checks and exception handling behave as expected.
 */
@ExtendWith(MockitoExtension.class)
class TechnologyValidatorTest {

    @Mock
    private TechnologyRepositoryPort repository;

    @InjectMocks
    private TechnologyValidator validator;

    private Technology existingTech;

    @BeforeEach
    void setup() {
        existingTech = new Technology(
                "Solar Panel",
                EnergyType.SOLAR,
                new Efficiency(0.85),
                new InstallationCost(BigDecimal.valueOf(1200)),
                new MaintenanceCost(BigDecimal.valueOf(100)),
                new EnvironmentalImpact(10.0),
                new Co2Reduction(BigDecimal.valueOf(250)),
                new EnergyProduction(5000));
    }

    // ============================================================
    // ensureUniqueName()
    // ============================================================
    @Test
    @DisplayName("Should pass when technology name is unique")
    void shouldPassWhenNameIsUnique() {
        when(repository.existsByName("Solar Panel")).thenReturn(false);

        assertDoesNotThrow(() -> validator.ensureUniqueName("Solar Panel"));

        verify(repository, times(1)).existsByName("Solar Panel");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when technology name already exists")
    void shouldThrowWhenNameExists() {
        when(repository.existsByName("Solar Panel")).thenReturn(true);

        var ex = assertThrows(IllegalArgumentException.class,
                () -> validator.ensureUniqueName("Solar Panel"));

        assertEquals("Technology with name 'Solar Panel' already exists", ex.getMessage());
        verify(repository, times(1)).existsByName("Solar Panel");
    }

    // ============================================================
    // ensureExists()
    // ============================================================
    @Test
    @DisplayName("Should pass when technology with given ID exists")
    void shouldPassWhenIdExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingTech));

        assertDoesNotThrow(() -> validator.ensureExists(1L));

        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw TechnologyNotFoundException when technology ID not found")
    void shouldThrowWhenIdNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        var ex = assertThrows(TechnologyNotFoundException.class,
                () -> validator.ensureExists(99L));

        assertEquals("Technology with ID 99 not found", ex.getMessage());
        verify(repository, times(1)).findById(99L);
    }

    // ============================================================
    // getExisting()
    // ============================================================
    @Test
    @DisplayName("Should return existing technology when found")
    void shouldReturnExistingTechnology() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingTech));

        Technology result = validator.getExisting(1L);

        assertNotNull(result);
        assertEquals("Solar Panel", result.getName());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw TechnologyNotFoundException when getExisting() not found")
    void shouldThrowWhenGetExistingNotFound() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        var ex = assertThrows(TechnologyNotFoundException.class,
                () -> validator.getExisting(10L));

        assertEquals("Technology with ID 10 not found", ex.getMessage());
        verify(repository, times(1)).findById(10L);
    }
}
