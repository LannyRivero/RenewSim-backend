package com.renewsim.backend.simulation_service.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SimulationRecordRepositoryPort {

    SimulationRecord save(SimulationRecord record);

    Optional<SimulationRecord> findById(Long id);

    List<SimulationRecord> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    void deleteById(Long id);

    void deleteAllByCreatedBy(String createdBy);

    record SimulationRecord(
            Long id,
            String name,
            String location,
            String energyType,
            Double locationLat,
            Double locationLng,
            Double projectSize,
            Double budget,
            Double estimatedEnergy,
            String climateData,
            Double co2Reduction,
            String createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String status,
            String modelVersion,
            String resourceSource,
            Double annualSavings,
            Double npv,
            Double irrPct,
            String recommendation,
            String inputSnapshot,
            String resultSnapshot) {
    }
}
