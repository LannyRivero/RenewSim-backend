package com.renewsim.backend.simulation_service.dashboard.application.projection;

import java.util.List;

public record PortfolioDashboardDistribution(
        List<PortfolioDashboardDistributionByTechnology> byTechnology,
        List<PortfolioDashboardDistributionByStatus> byStatus) {
}
