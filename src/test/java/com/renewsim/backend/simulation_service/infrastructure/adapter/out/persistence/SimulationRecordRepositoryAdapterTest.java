package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationStatus;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.infrastructure.persistence.entity.SimulationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationRecordRepositoryAdapterTest {

    @Mock
    private JpaSimulationRepository repository;

    @Mock
    private SimulationRecordEntityMapper entityMapper;

    @Test
    @DisplayName("save assigns generated id back to new simulation")
    void saveAssignsGeneratedIdBackToNewSimulation() {
        Simulation simulation = draftSimulation();
        SimulationEntity entity = new SimulationEntity();
        SimulationEntity saved = new SimulationEntity();
        saved.setId(55L);

        when(entityMapper.toEntity(simulation)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);

        SimulationRecordRepositoryAdapter adapter = new SimulationRecordRepositoryAdapter(repository, entityMapper);
        Simulation result = adapter.save(simulation);

        assertThat(result.getId().value()).isEqualTo(55L);
        verify(entityMapper).toEntity(simulation);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("findById maps entity through mapper")
    void findByIdMapsEntityThroughMapper() {
        SimulationEntity entity = new SimulationEntity();
        Simulation simulation = existingSimulation(88L);
        when(repository.findById(88L)).thenReturn(Optional.of(entity));
        when(entityMapper.toDomain(entity)).thenReturn(simulation);

        SimulationRecordRepositoryAdapter adapter = new SimulationRecordRepositoryAdapter(repository, entityMapper);
        Optional<Simulation> result = adapter.findById(88L);

        assertThat(result).contains(simulation);
        verify(repository).findById(88L);
        verify(entityMapper).toDomain(entity);
    }

    @Test
    @DisplayName("findByCreatedByOrderByCreatedAtDesc maps all returned entities")
    void findByCreatedByOrderByCreatedAtDescMapsAllReturnedEntities() {
        SimulationEntity first = new SimulationEntity();
        SimulationEntity second = new SimulationEntity();
        Simulation firstSimulation = existingSimulation(55L);
        Simulation secondSimulation = existingSimulation(56L);
        when(repository.findByCreatedByOrderByCreatedAtDesc("alice")).thenReturn(List.of(first, second));
        when(entityMapper.toDomain(first)).thenReturn(firstSimulation);
        when(entityMapper.toDomain(second)).thenReturn(secondSimulation);

        SimulationRecordRepositoryAdapter adapter = new SimulationRecordRepositoryAdapter(repository, entityMapper);
        List<Simulation> result = adapter.findByCreatedByOrderByCreatedAtDesc("alice");

        assertThat(result).containsExactly(firstSimulation, secondSimulation);
        verify(repository).findByCreatedByOrderByCreatedAtDesc("alice");
        verify(entityMapper).toDomain(first);
        verify(entityMapper).toDomain(second);
    }

    @Test
    @DisplayName("delete methods delegate directly to repository")
    void deleteMethodsDelegateDirectlyToRepository() {
        SimulationRecordRepositoryAdapter adapter = new SimulationRecordRepositoryAdapter(repository, entityMapper);

        adapter.deleteById(55L);
        adapter.deleteAllByCreatedBy("alice");

        verify(repository).deleteById(55L);
        verify(repository).deleteAllByCreatedBy("alice");
    }

    private Simulation draftSimulation() {
        return Simulation.create(
                "Solar - Sevilla",
                Technology.solar(),
                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", CountryCode.of("ES")),
                new SimulationSystem(300.0, 0.81, 0.5, 99.0, new SimulationSystem.LossesPct(2.0, 6.0, 1.0, 3.0, 1.0)),
                ConsumptionProfile.of(120000,
                        List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                10000d)),
                new SimulationEconomics(Currency.of("EUR"), 315000.0, 7200.0, 0.18, 0.07, 8, ProjectLifetime.of(20)),
                "alice");
    }

    private Simulation existingSimulation(Long id) {
        return Simulation.reconstitute(
                id,
                "Solar - Sevilla",
                Technology.solar(),
                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", CountryCode.of("ES")),
                new SimulationSystem(300.0, 0.81, 0.5, 99.0, new SimulationSystem.LossesPct(2.0, 6.0, 1.0, 3.0, 1.0)),
                ConsumptionProfile.of(120000,
                        List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                10000d)),
                new SimulationEconomics(Currency.of("EUR"), 315000.0, 7200.0, 0.18, 0.07, 8, ProjectLifetime.of(20)),
                SimulationStatus.COMPLETED,
                "{}",
                457200.0,
                68700.0,
                121500.0,
                11.4,
                "viable_with_reservations",
                "alice",
                LocalDateTime.parse("2026-06-30T14:00:00"),
                LocalDateTime.parse("2026-06-30T14:30:00"));
    }
}
