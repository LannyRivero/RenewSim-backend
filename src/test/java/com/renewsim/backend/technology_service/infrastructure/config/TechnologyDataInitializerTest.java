package com.renewsim.backend.technology_service.infrastructure.config;

import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.domain.model.Technology;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnologyDataInitializerTest {

    @Mock
    private TechnologyRepositoryPort repository;

    @Test
    @DisplayName("run should insert default technologies when repository is empty")
    void runShouldInsertDefaultTechnologiesWhenRepositoryIsEmpty() throws Exception {
        when(repository.findAll()).thenReturn(List.of());
        when(repository.existsByName(any())).thenReturn(false);

        TechnologyDataInitializer initializer = new TechnologyDataInitializer(repository);

        initializer.run();

        ArgumentCaptor<Technology> captor = ArgumentCaptor.forClass(Technology.class);
        verify(repository, times(3)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(Technology::getName)
                .containsExactly("Solar Panel", "Wind Turbine", "Hydro Generator");
    }

    @Test
    @DisplayName("run should skip initialization when repository already has technologies")
    void runShouldSkipInitializationWhenRepositoryAlreadyHasTechnologies() throws Exception {
        when(repository.findAll()).thenReturn(List.of(org.mockito.Mockito.mock(Technology.class)));

        TechnologyDataInitializer initializer = new TechnologyDataInitializer(repository);

        initializer.run();

        verify(repository, never()).existsByName(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("run should skip duplicates when a default technology already exists")
    void runShouldSkipDuplicatesWhenADefaultTechnologyAlreadyExists() throws Exception {
        when(repository.findAll()).thenReturn(List.of());
        when(repository.existsByName("Solar Panel")).thenReturn(true);
        when(repository.existsByName("Wind Turbine")).thenReturn(false);
        when(repository.existsByName("Hydro Generator")).thenReturn(false);

        TechnologyDataInitializer initializer = new TechnologyDataInitializer(repository);

        initializer.run();

        ArgumentCaptor<Technology> captor = ArgumentCaptor.forClass(Technology.class);
        verify(repository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(Technology::getName)
                .containsExactly("Wind Turbine", "Hydro Generator");
    }
}
