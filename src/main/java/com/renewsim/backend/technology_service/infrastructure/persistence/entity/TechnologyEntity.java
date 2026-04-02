package com.renewsim.backend.technology_service.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "technologies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnologyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "energy_type", nullable = false)
    private EnergyType energyType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "unit_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "maintenance_cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal maintenanceCost;

    @Column(name = "lifespan_years", nullable = false)
    private Integer lifespanYears;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal efficiency;

    @Column(name = "capacity_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal capacityFactor;

    @Column(name = "min_capacity_kw", nullable = false, precision = 10, scale = 2)
    private BigDecimal minCapacityKw;

    @Column(name = "max_capacity_kw", precision = 10, scale = 2)
    private BigDecimal maxCapacityKw;

    @Column(name = "co2_reduction_factor", nullable = false, precision = 10, scale = 4)
    private BigDecimal co2ReductionFactor;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum EnergyType {
        SOLAR, WIND, HYDRO, GEOTHERMAL, BIOMASS
    }
}