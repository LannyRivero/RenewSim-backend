package com.renewsim.backend.simulation_service.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
    @DisplayName("V15 should create simulation_technologies foreign key to simulations")
    void shouldCreateSimulationTechnologiesForeignKey() throws Exception {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                     SELECT COUNT(*)
                     FROM information_schema.referential_constraints
                     WHERE constraint_schema = DATABASE()
                       AND table_name = 'simulation_technologies'
                       AND referenced_table_name = 'simulations'
                     """)) {
            try (ResultSet rs = statement.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
        }
    }
}
