package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "simulations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String location;

    @Column(name = "location_name", insertable = false, updatable = false)
    private String locationName;

    @Column(name = "energy_type", nullable = false)
    private String energyType;

    @Column(name = "location_lat", nullable = false)
    private Double locationLat;

    @Column(name = "location_lng", nullable = false)
    private Double locationLng;

    @Column(name = "project_size", nullable = false)
    private Double projectSize;

    @Column(name = "capacity_kw", nullable = false)
    private Double capacityKw;

    @Column(nullable = false)
    private Double budget;

    @Column(name = "initial_investment", nullable = false)
    private Double initialInvestment;

    @Column(name = "total_cost", nullable = false)
    private Double totalCost;

    @Column(name = "estimated_energy", nullable = false)
    private Double estimatedEnergy;

    @Column(name = "energy_generated", insertable = false, updatable = false)
    private Double energyGenerated;

    @Column(name = "annual_energy_generated", insertable = false, updatable = false)
    private Double annualEnergyGenerated;

    @Column(name = "climate_data")
    private String climateData;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "status")
    private String status;

    @Column(name = "annual_savings")
    private Double annualSavings;

    @Column(name = "npv")
    private Double npv;

    @Column(name = "irr_pct")
    private Double irrPct;

    @Column(name = "recommendation")
    private String recommendation;

    @Column(name = "scenario_id")
    private Long scenarioId;

    @Lob
    @Column(name = "input_snapshot", columnDefinition = "LONGTEXT")
    private String inputSnapshot;

    @Lob
    @Column(name = "result_snapshot", columnDefinition = "LONGTEXT")
    private String resultSnapshot;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "simulation_technologies", joinColumns = @JoinColumn(name = "simulation_id"))
    @Column(name = "technology_id")
    @Builder.Default
    private List<Long> technologyIds = new ArrayList<>();
}
