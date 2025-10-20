package com.renewsim.backend.simulation_service.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 🧱 SimulationEntity
 *
 * JPA entity representing a Simulation persisted in the database.
 * Contains only persistence structure, no business logic.
 *
 * 💡 Notes:
 * - "userId" is a logical reference to the owning user (no @ManyToOne relation).
 * - "technologyIds" is stored as an ElementCollection for flexibility.
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

    /**
     * Logical link to the user that owns this simulation.
     * No foreign key constraint — keeps microservices decoupled.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * List of related technologies (IDs from technology_service).
     * Uses an element collection for simplicity and JSON-friendly structure.
     */
    @ElementCollection
    @CollectionTable(name = "simulation_technologies", joinColumns = @JoinColumn(name = "simulation_id"))
    @Column(name = "technology_id")
    private List<Long> technologyIds;

    private LocalDateTime createdAt;
}
