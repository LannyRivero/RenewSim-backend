package com.renewsim.backend.simulation_service.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

final class SimulationMigrationAssertions {

    static final String EXPECTED_FLYWAY_VERSION = "27";

    private SimulationMigrationAssertions() {
    }

    static void assertSchemaReadiness(Connection connection) throws Exception {
        assertThat(countMatchingColumns(connection)).isEqualTo(6);
        assertThat(tableExists(connection, "simulation_technologies")).isTrue();
        assertThat(hasSimulationTechnologiesToSimulationsForeignKey(connection)).isTrue();
    }

    static void assertFlywayVersionApplied(Connection connection, String expectedVersion) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = ?
                          AND success = 1
                        """)) {
            statement.setString(1, expectedVersion);
            try (ResultSet rs = statement.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
        }
    }

    private static int countMatchingColumns(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                """
                        SELECT COUNT(*)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = 'simulations'
                          AND column_name IN ('location', 'energy_type', 'project_size', 'budget', 'estimated_energy', 'created_by')
                        """)) {
            try (ResultSet rs = statement.executeQuery()) {
                assertThat(rs.next()).isTrue();
                return rs.getInt(1);
            }
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = DATABASE()
                          AND table_name = ?
                        """)) {
            statement.setString(1, tableName);
            try (ResultSet rs = statement.executeQuery()) {
                assertThat(rs.next()).isTrue();
                return rs.getInt(1) == 1;
            }
        }
    }

    private static boolean hasSimulationTechnologiesToSimulationsForeignKey(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                """
                        SELECT COUNT(*)
                        FROM information_schema.referential_constraints
                        WHERE constraint_schema = DATABASE()
                          AND table_name = 'simulation_technologies'
                          AND referenced_table_name = 'simulations'
                        """)) {
            try (ResultSet rs = statement.executeQuery()) {
                assertThat(rs.next()).isTrue();
                return rs.getInt(1) == 1;
            }
        }
    }
}
