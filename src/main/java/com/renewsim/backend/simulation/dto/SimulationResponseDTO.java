package com.renewsim.backend.simulation.dto;
import java.time.LocalDateTime;
import java.util.List;

import com.renewsim.backend.technologyComparison.dto.TechnologyComparisonResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimulationResponseDTO {
    private Long simulationId;
    private String location;                  
    private String energyType; 
    private double energyGenerated;
    private double estimatedSavings;
    private double returnOnInvestment;
    private double projectSize;
    private double budget;
    private LocalDateTime timestamp; 
    private List<TechnologyComparisonResponseDTO> technologies;
    private String recommendedTechnology;
}




