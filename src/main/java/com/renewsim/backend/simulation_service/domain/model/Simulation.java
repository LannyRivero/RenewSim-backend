package com.renewsim.backend.simulation_service.domain.model;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationCompletionException;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationCreatorException;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationIdentityAssignmentException;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationNameException;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationStatusTransitionException;
import com.renewsim.backend.simulation_service.domain.model.vo.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Simulation {

    private SimulationId id;
    private final String name;
    private final Technology technology;
    private final SimulationLocation location;
    private final SimulationSystem system;
    private final ConsumptionProfile demand;
    private final SimulationEconomics economics;
    private SimulationStatus status;
    private String resultSnapshot;
    private Double annualGenerationKwh;
    private Double annualSavings;
    private Double npv;
    private Double irrPct;
    private String recommendation;
    private List<Long> technologyIds;
    private Long scenarioId;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Simulation(
            SimulationId id, String name, Technology technology,
            SimulationLocation location, SimulationSystem system,
            ConsumptionProfile demand, SimulationEconomics economics,
            SimulationStatus status, String resultSnapshot,
            Double annualGenerationKwh, Double annualSavings,
            Double npv, Double irrPct, String recommendation,
            List<Long> technologyIds, Long scenarioId,
            String createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {

        validateName(name);
        validateCreator(createdBy);

        this.id = id;
        this.name = name;
        this.technology = technology;
        this.location = location;
        this.system = system;
        this.demand = demand;
        this.economics = economics;
        this.status = status;
        this.resultSnapshot = resultSnapshot;
        this.annualGenerationKwh = annualGenerationKwh;
        this.annualSavings = annualSavings;
        this.npv = npv;
        this.irrPct = irrPct;
        this.recommendation = recommendation;
        this.technologyIds = technologyIds == null ? new ArrayList<>() : new ArrayList<>(technologyIds);
        this.scenarioId = scenarioId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ========== FACTORY ==========

    public static Simulation create(
            String name, Technology technology,
            SimulationLocation location, SimulationSystem system,
            ConsumptionProfile demand, SimulationEconomics economics,
            List<Long> technologyIds, Long scenarioId,
            String createdBy) {
        LocalDateTime now = LocalDateTime.now();
        return new Simulation(
                null, name, technology, location, system, demand, economics,
                SimulationStatus.DRAFT, null,
                null, null, null, null, null,
                technologyIds, scenarioId,
                createdBy, now, now);
    }

    public static Simulation reconstitute(
            Long id, String name, Technology technology,
            SimulationLocation location, SimulationSystem system,
            ConsumptionProfile demand, SimulationEconomics economics,
            SimulationStatus status, String resultSnapshot,
            Double annualGenerationKwh, Double annualSavings,
            Double npv, Double irrPct, String recommendation,
            List<Long> technologyIds, Long scenarioId,
            String createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Simulation(
                id == null ? null : SimulationId.of(id), name, technology, location, system, demand, economics,
                status, resultSnapshot,
                annualGenerationKwh, annualSavings, npv, irrPct, recommendation,
                technologyIds, scenarioId,
                createdBy, createdAt, updatedAt);
    }

    // ========== STATE TRANSITIONS ==========

    public void complete(SimulationCompletion completion) {
        if (status != SimulationStatus.DRAFT) {
            throw new InvalidSimulationStatusTransitionException("complete", status);
        }
        if (completion == null) {
            throw new InvalidSimulationCompletionException("Simulation completion is required");
        }
        this.resultSnapshot = completion.resultSnapshot();
        this.annualGenerationKwh = completion.annualGenerationKwh();
        this.annualSavings = completion.annualSavings();
        this.npv = completion.npv();
        this.irrPct = completion.irrPct();
        this.recommendation = completion.recommendation();
        if (completion.technologyIds() != null) {
            this.technologyIds = new ArrayList<>(completion.technologyIds());
        }
        this.status = SimulationStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        if (status.isTerminal()) {
            throw new InvalidSimulationStatusTransitionException("delete", status);
        }
        this.status = SimulationStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    // ========== QUERIES ==========

    public boolean isOwnedBy(String username) {
        return createdBy.equalsIgnoreCase(username);
    }

    public boolean isCompleted() {
        return status == SimulationStatus.COMPLETED;
    }

    public boolean hasResult() {
        return resultSnapshot != null && !resultSnapshot.isBlank();
    }

    // ========== SETTERS ==========

    public void assignId(SimulationId id) {
        if (this.id != null) {
            throw new InvalidSimulationIdentityAssignmentException();
        }
        this.id = id;
    }

    public void assignTechnologyIds(List<Long> technologyIds) {
        this.technologyIds = technologyIds == null ? new ArrayList<>() : new ArrayList<>(technologyIds);
    }

    public void assignScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }

    // ========== GETTERS ==========

    public SimulationId getId() { return id; }
    public String getName() { return name; }
    public Technology getTechnology() { return technology; }
    public SimulationLocation getLocation() { return location; }
    public SimulationSystem getSystem() { return system; }
    public ConsumptionProfile getDemand() { return demand; }
    public SimulationEconomics getEconomics() { return economics; }
    public SimulationStatus getStatus() { return status; }
    public String getResultSnapshot() { return resultSnapshot; }
    public Double getAnnualGenerationKwh() { return annualGenerationKwh; }
    public Double getAnnualSavings() { return annualSavings; }
    public Double getNpv() { return npv; }
    public Double getIrrPct() { return irrPct; }
    public String getRecommendation() { return recommendation; }
    public List<Long> getTechnologyIds() { return List.copyOf(technologyIds); }
    public Long getScenarioId() { return scenarioId; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidSimulationNameException("Simulation name must not be blank");
        }
    }

    private static void validateCreator(String createdBy) {
        if (createdBy == null || createdBy.isBlank()) {
            throw new InvalidSimulationCreatorException("Simulation creator must not be blank");
        }
    }

}
