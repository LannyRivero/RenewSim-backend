package com.renewsim.backend.simulation_service.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA entity representing a Simulation persisted in the database.
 * No business logic — only persistence structure.
 */
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

    private String location;
    private String energyType;
    private double projectSize;
    private double budget;
    private double estimatedEnergy;
    private double co2Reduction;

    @ElementCollection
    @CollectionTable(name = "simulation_technologies", joinColumns = @JoinColumn(name = "simulation_id"))
    @Column(name = "technology_id")
    private List<Long> technologyIds;

    private LocalDateTime createdAt;
}
