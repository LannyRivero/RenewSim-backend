package com.renewsim.backend.simulation_service.application.dashboard;

/**
 * Produces the executive dashboard for a user's simulation portfolio.
 */
public interface GetPortfolioDashboardUseCase {

    /**
     * Builds the current dashboard snapshot for the given user.
     */
    PortfolioDashboardResult getDashboard(String username);
}
