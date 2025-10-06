package com.renewsim.backend.technology_service.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

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

    private String name;
    private double efficiency;
    private double installationCost;
    private double maintenanceCost;
    private double environmentalImpact;
    private double co2Reduction;
    private double energyProduction;
    private String energyType;
}
