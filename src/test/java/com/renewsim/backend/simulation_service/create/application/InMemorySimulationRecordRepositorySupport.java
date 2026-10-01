package com.renewsim.backend.simulation_service.create.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordRepositoryAdapter;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

final class InMemorySimulationRecordRepositorySupport {

        private InMemorySimulationRecordRepositorySupport() {
        }

        static SimulationRecordRepositoryAdapter create(JpaSimulationRepository jpaRepository) {
                Map<Long, SimulationEntity> store = new HashMap<>();
                AtomicLong sequence = new AtomicLong(60L);

                when(jpaRepository.save(any(SimulationEntity.class))).thenAnswer(invocation -> {
                        SimulationEntity entity = invocation.getArgument(0);
                        SimulationEntity stored = copyEntity(entity);
                        if (stored.getId() == null) {
                                stored.setId(sequence.getAndIncrement());
                        }
                        store.put(stored.getId(), stored);
                        return copyEntity(stored);
                });
                when(jpaRepository.findById(any(Long.class))).thenAnswer(invocation -> {
                        Long id = invocation.getArgument(0);
                        SimulationEntity stored = store.get(id);
                        return Optional.ofNullable(stored == null ? null : copyEntity(stored));
                });
                when(jpaRepository.findByCreatedByAndStatusNotOrderByCreatedAtDesc("alice", "DELETED"))
                                .thenAnswer(invocation -> store.values()
                                                .stream()
                                                .filter(entity -> "alice".equals(entity.getCreatedBy()))
                                                .filter(entity -> !"DELETED".equalsIgnoreCase(entity.getStatus()))
                                                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                                                .map(InMemorySimulationRecordRepositorySupport::copyEntity)
                                                .toList());

                return new SimulationRecordRepositoryAdapter(jpaRepository,
                                new ObjectMapper().findAndRegisterModules());
        }

        private static SimulationEntity copyEntity(SimulationEntity source) {
                SimulationEntity copy = new SimulationEntity();
                copy.setId(source.getId());
                copy.setName(source.getName());
                copy.setLocation(source.getLocation());
                copy.setEnergyType(source.getEnergyType());
                copy.setLocationLat(source.getLocationLat());
                copy.setLocationLng(source.getLocationLng());
                copy.setProjectSize(source.getProjectSize());
                copy.setBudget(source.getBudget());
                copy.setEstimatedEnergy(source.getEstimatedEnergy());
                copy.setClimateData(source.getClimateData());
                copy.setCreatedBy(source.getCreatedBy());
                copy.setCreatedAt(source.getCreatedAt());
                copy.setUpdatedAt(source.getUpdatedAt());
                copy.setStatus(source.getStatus());
                copy.setAnnualSavings(source.getAnnualSavings());
                copy.setNpv(source.getNpv());
                copy.setIrrPct(source.getIrrPct());
                copy.setRecommendation(source.getRecommendation());
                copy.setScenarioId(source.getScenarioId());
                copy.setInputSnapshot(source.getInputSnapshot());
                copy.setResultSnapshot(source.getResultSnapshot());
                copy.setTechnologyIds(
                                source.getTechnologyIds() == null ? List.of() : List.copyOf(source.getTechnologyIds()));
                return copy;
        }
}
