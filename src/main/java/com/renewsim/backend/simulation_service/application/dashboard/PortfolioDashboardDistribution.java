package com.renewsim.backend.simulation_service.application.dashboard;

import java.util.List;

public record PortfolioDashboardDistribution(
        List<PortfolioDashboardDistributionByTechnology> byTechnology,
        List<PortfolioDashboardDistributionByStatus> byStatus) {
}
