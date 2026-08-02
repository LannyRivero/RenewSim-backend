package com.renewsim.backend.simulation_service.dashboard.application.port.in;

import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardResult;

/**
 * Produces the executive dashboard for a user's simulation portfolio.
 */
public interface GetPortfolioDashboardUseCase {

    /**
     * Builds the current dashboard snapshot for the given user.
     */
    PortfolioDashboardResult getDashboard(String username);
}
