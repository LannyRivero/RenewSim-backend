package com.renewsim.backend.simulation_service.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulationSchemaMigrationIT {

    private Flyway buildFlyway(String jdbcUrl, String user, String pass) {
        return Flyway.configure()
                .dataSource(jdbcUrl, user, pass)
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();
    }

    private Flyway buildFlywayWithBrokenMigration(String jdbcUrl, String user, String pass) {
        return Flyway.configure()
                .dataSource(jdbcUrl, user, pass)
                .locations(
                        "classpath:db/migration",
                        "classpath:db/migration_broken")
                .cleanDisabled(false)
                .load();
    }

    private Connection openConnection(String jdbcUrl, String user, String pass) throws Exception {
        return DriverManager.getConnection(jdbcUrl, user, pass);
    }

    private String mysqlAdminUrl() {
        return System.getProperty("test.db.admin.url", "jdbc:mysql://localhost:3306/mysql");
    }

    private String mysqlHostPort() {
        return System.getProperty("test.db.hostport", "localhost:3306");
    }

    private String mysqlUser() {
        return System.getProperty("test.db.user", "root");
    }

    private String mysqlPass() {
        return System.getProperty("test.db.pass", "root");
    }

    private String createIsolatedDatabase() throws Exception {
        String dbName = "renewsim_migration_it_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(mysqlAdminUrl(), mysqlUser(), mysqlPass());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        return dbName;
    }

    private void dropIsolatedDatabase(String dbName) throws Exception {
        try (Connection connection = DriverManager.getConnection(mysqlAdminUrl(), mysqlUser(), mysqlPass());
             Statement statement = connection.createStatement()) {
            statement.execute("DROP DATABASE IF EXISTS `" + dbName + "`");
        }
    }

    private String isolatedJdbcUrl(String dbName) {
        return "jdbc:mysql://" + mysqlHostPort() + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
    }

    @Test
    @DisplayName("Flyway migrate should prepare simulation schema readiness on isolated DB lifecycle")
    void shouldApplyMigrationChainAndExposeSimulationSchemaReadiness() throws Exception {
        String dbName = createIsolatedDatabase();
        String jdbcUrl = isolatedJdbcUrl(dbName);
        try {
            Flyway flyway = buildFlyway(jdbcUrl, mysqlUser(), mysqlPass());
            flyway.clean();

            MigrateResult result = flyway.migrate();
            assertThat(result.success).isTrue();
            assertThat(result.migrationsExecuted).isGreaterThan(0);

            assertSchemaReadiness(jdbcUrl, mysqlUser(), mysqlPass());
            assertFlywayVersionApplied(jdbcUrl, mysqlUser(), mysqlPass(), "15");
        } finally {
            dropIsolatedDatabase(dbName);
        }
    }

    @Test
    @DisplayName("Flyway migrate should be reproducible across isolated clean lifecycles")
    void shouldRemainDeterministicAcrossRepeatedIsolatedRuns() throws Exception {
        String dbName = createIsolatedDatabase();
        String jdbcUrl = isolatedJdbcUrl(dbName);
        try {
            Flyway flyway = buildFlyway(jdbcUrl, mysqlUser(), mysqlPass());

            flyway.clean();
            MigrateResult firstRun = flyway.migrate();
            assertThat(firstRun.success).isTrue();
            int firstRunExecuted = firstRun.migrationsExecuted;

            flyway.clean();
            MigrateResult secondRun = flyway.migrate();
            assertThat(secondRun.success).isTrue();
            assertThat(secondRun.migrationsExecuted).isEqualTo(firstRunExecuted);

            assertSchemaReadiness(jdbcUrl, mysqlUser(), mysqlPass());
        } finally {
            dropIsolatedDatabase(dbName);
        }
    }

    @Test
    @DisplayName("Flyway migrate should fail when migration chain includes broken script")
    void shouldFailWhenMigrationChainIsBroken() throws Exception {
        String dbName = createIsolatedDatabase();
        String jdbcUrl = isolatedJdbcUrl(dbName);
        try {
            Flyway flyway = buildFlywayWithBrokenMigration(jdbcUrl, mysqlUser(), mysqlPass());
            flyway.clean();

            assertThatThrownBy(flyway::migrate)
                    .isInstanceOf(FlywayException.class)
                    .hasMessageContaining("V999");
        } finally {
            dropIsolatedDatabase(dbName);
        }
    }

    private void assertSchemaReadiness(String jdbcUrl, String user, String pass) throws Exception {
        try (Connection connection = openConnection(jdbcUrl, user, pass)) {
            assertThat(countMatchingColumns(connection)).isEqualTo(6);
            assertThat(tableExists(connection, "simulation_technologies")).isTrue();
            assertThat(hasSimulationTechnologiesToSimulationsForeignKey(connection)).isTrue();
        }
    }

    private int countMatchingColumns(Connection connection) throws Exception {
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

    private boolean tableExists(Connection connection, String tableName) throws Exception {
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

    private boolean hasSimulationTechnologiesToSimulationsForeignKey(Connection connection) throws Exception {
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

    private void assertFlywayVersionApplied(String jdbcUrl, String user, String pass, String expectedVersion)
            throws Exception {
        try (Connection connection = openConnection(jdbcUrl, user, pass);
             PreparedStatement statement = connection.prepareStatement(
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
}
