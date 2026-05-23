package com.renewsim.backend.simulation_service.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                     SELECT COUNT(*)
                     FROM information_schema.columns
                     WHERE table_schema = DATABASE()
                       AND table_name = 'simulations'
                       AND column_name IN ('location', 'energy_type', 'project_size', 'budget', 'estimated_energy', 'created_by')
                     """)) {
            try (ResultSet rs = statement.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(6);
            }
        }
    }

    @Test
    @DisplayName("V15 should create simulation_technologies table")
    void shouldCreateSimulationTechnologiesTable() throws Exception {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                     SELECT COUNT(*)
                     FROM information_schema.tables
                     WHERE table_schema = DATABASE()
                       AND table_name = 'simulation_technologies'
                     """)) {
            try (ResultSet rs = statement.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
        }
    }

    @Test
    @DisplayName("V15 should allow baseline simulation insert with legacy nullable fields")
    void shouldAllowBaselineInsertWithLegacyNulls() throws Exception {
        String location = "migration-test-" + UUID.randomUUID();
        try (Connection connection = openConnection()) {
            long simulationId;
            try (PreparedStatement insertSimulation = connection.prepareStatement(
                    """
                    INSERT INTO simulations (location, energy_type, project_size, budget, estimated_energy, co2_reduction, created_by)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """)) {
                insertSimulation.setString(1, location);
                insertSimulation.setString(2, "SOLAR");
                insertSimulation.setDouble(3, 120.0);
                insertSimulation.setDouble(4, 50000.0);
                insertSimulation.setDouble(5, 150000.0);
                insertSimulation.setDouble(6, 40.0);
                insertSimulation.setString(7, "tester");
                assertThat(insertSimulation.executeUpdate()).isEqualTo(1);
            }

            try (PreparedStatement selectSimulation = connection.prepareStatement(
                    "SELECT id FROM simulations WHERE location = ? ORDER BY id DESC LIMIT 1")) {
                selectSimulation.setString(1, location);
                try (ResultSet rs = selectSimulation.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    simulationId = rs.getLong(1);
                }
            }

            try (PreparedStatement insertRelation = connection.prepareStatement(
                    "INSERT INTO simulation_technologies (simulation_id, technology_id) VALUES (?, ?)");
                 PreparedStatement deleteRelation = connection.prepareStatement(
                         "DELETE FROM simulation_technologies WHERE simulation_id = ?");
                 PreparedStatement deleteSimulation = connection.prepareStatement(
                         "DELETE FROM simulations WHERE id = ?")) {
                insertRelation.setLong(1, simulationId);
                insertRelation.setLong(2, 999L);
                assertThat(insertRelation.executeUpdate()).isEqualTo(1);

                deleteRelation.setLong(1, simulationId);
                deleteRelation.executeUpdate();

                deleteSimulation.setLong(1, simulationId);
                deleteSimulation.executeUpdate();
            }
        }
    }
}
