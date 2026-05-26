package com.renewsim.backend.simulation_service.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

class SimulationSchemaMigrationTest {

    private Connection openConnection() throws Exception {
        String url = System.getProperty("test.db.url", "jdbc:mysql://localhost:3306/renewsim");
        String user = System.getProperty("test.db.user", "root");
        String pass = System.getProperty("test.db.pass", "root");
        return DriverManager.getConnection(url, user, pass);
    }

    @Test
    @DisplayName("V15 should add aligned simulation columns")
    void shouldExposeV15AlignedColumns() throws Exception {
        // Keep this test suite as smoke checks; SQL contract is centralized in SimulationMigrationAssertions.
        try (Connection connection = openConnection()) {
            SimulationMigrationAssertions.assertSchemaReadiness(connection);
        }
    }

    @Test
    @DisplayName("V15 should create simulation_technologies table")
    void shouldCreateSimulationTechnologiesTable() throws Exception {
        try (Connection connection = openConnection()) {
            SimulationMigrationAssertions.assertSchemaReadiness(connection);
        }
    }

    @Test
    @DisplayName("V15 should create simulation_technologies foreign key to simulations")
    void shouldCreateSimulationTechnologiesForeignKey() throws Exception {
        try (Connection connection = openConnection()) {
            SimulationMigrationAssertions.assertSchemaReadiness(connection);
        }
    }

    @Test
    @DisplayName("V15 should be registered as applied")
    void shouldRegisterFlywayVersionV15AsApplied() throws Exception {
        try (Connection connection = openConnection()) {
            SimulationMigrationAssertions.assertFlywayVersionApplied(
                    connection,
                    SimulationMigrationAssertions.EXPECTED_FLYWAY_VERSION);
        }
    }
}
