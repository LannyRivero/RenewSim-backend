package com.renewsim.backend.simulation_service.dashboard.web.dto;

import java.util.List;

public record PortfolioDashboardDistributionDTO(
                List<PortfolioDashboardDistributionByTechnologyDTO> byTechnology,
                List<PortfolioDashboardDistributionByStatusDTO> byStatus) {
}
