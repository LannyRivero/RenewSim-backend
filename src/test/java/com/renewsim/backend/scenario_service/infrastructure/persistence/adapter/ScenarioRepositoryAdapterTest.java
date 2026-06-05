package com.renewsim.backend.scenario_service.infrastructure.persistence.adapter;

import com.renewsim.backend.scenario_service.domain.model.Scenario;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultCapacityKw;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultConsumption;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultTariff;
import com.renewsim.backend.scenario_service.domain.model.vo.ScenarioTechnologyId;
import com.renewsim.backend.scenario_service.infrastructure.mapper.ScenarioMapper;
import com.renewsim.backend.scenario_service.infrastructure.persistence.entity.ScenarioEntity;
import com.renewsim.backend.scenario_service.infrastructure.persistence.repository.JpaScenarioRepository;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import com.renewsim.backend.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioRepositoryAdapterTest {

    @Mock
    private JpaScenarioRepository jpaRepository;

    @Mock
    private ScenarioMapper mapper;

    @InjectMocks
    private ScenarioRepositoryAdapter adapter;

    @Test
    @DisplayName("save should persist scenario through mapper and repository")
    void saveShouldPersistScenarioThroughMapperAndRepository() {
        Scenario scenario = new Scenario(
                "Residential Solar",
                "Default home solar setup",
                5L,
                5.0,
                new Money(new BigDecimal("7500.00"), "USD"),
                0.15,
                6000.0,
                new ClimateData(5.5, 3.2, 22.0));
        ScenarioEntity entity = ScenarioEntity.builder().name("Residential Solar").build();
        ScenarioEntity savedEntity = ScenarioEntity.builder().id(1L).name("Residential Solar").build();
        Scenario savedScenario = new Scenario(
                1L,
                "Residential Solar",
                "Default home solar setup",
                new ScenarioTechnologyId(5L),
                new DefaultCapacityKw(5.0),
                new Money(new BigDecimal("7500.00"), "USD"),
                new DefaultTariff(0.15),
                new DefaultConsumption(6000.0),
                new ClimateData(5.5, 3.2, 22.0),
                true);

        when(mapper.toEntity(scenario)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedScenario);

        var result = adapter.save(scenario);

        assertThat(result.getId()).isEqualTo(1L);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("findAllActive should return mapped active scenarios")
    void findAllActiveShouldReturnMappedActiveScenarios() {
        ScenarioEntity entity = ScenarioEntity.builder().id(2L).name("Industrial Wind").build();
        Scenario scenario = new Scenario(
                2L,
                "Industrial Wind",
                "Default wind setup",
                new ScenarioTechnologyId(9L),
                new DefaultCapacityKw(50.0),
                new Money(new BigDecimal("120000.00"), "USD"),
                new DefaultTariff(0.12),
                new DefaultConsumption(150000.0),
                new ClimateData(4.0, 9.5, 18.0),
                true);

        when(jpaRepository.findByIsActiveTrue()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(scenario);

        var result = adapter.findAllActive();

        assertThat(result).containsExactly(scenario);
        verify(jpaRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("findById should return mapped scenario when present")
    void findByIdShouldReturnMappedScenarioWhenPresent() {
        ScenarioEntity entity = ScenarioEntity.builder().id(3L).name("Scenario").build();
        Scenario scenario = new Scenario(
                3L,
                "Scenario",
                null,
                new ScenarioTechnologyId(1L),
                new DefaultCapacityKw(1.0),
                new Money(new BigDecimal("100.00"), "USD"),
                new DefaultTariff(0.10),
                new DefaultConsumption(100.0),
                new ClimateData(1.0, 2.0, 3.0),
                true);

        when(jpaRepository.findById(3L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(scenario);

        var result = adapter.findById(3L);

        assertThat(result).contains(scenario);
        verify(jpaRepository).findById(3L);
    }
}
