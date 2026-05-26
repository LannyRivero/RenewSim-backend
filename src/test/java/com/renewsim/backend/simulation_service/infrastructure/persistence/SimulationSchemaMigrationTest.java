package com.renewsim.backend.simulation_service.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;

class SimulationSchemaMigrationTest {

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

    private Flyway buildFlyway(String jdbcUrl) {
        return Flyway.configure()
                .dataSource(jdbcUrl, mysqlUser(), mysqlPass())
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();
    }

    private Connection openConnection(String jdbcUrl) throws Exception {
        return DriverManager.getConnection(jdbcUrl, mysqlUser(), mysqlPass());
    }

    private MigrationTestContext createMigratedContext(String testName) throws Exception {
        FallbackDatabaseLifecycle lifecycle = new FallbackDatabaseLifecycle(
                mysqlAdminUrl(),
                mysqlHostPort(),
                mysqlUser(),
                mysqlPass());
        String dbId = lifecycle.createIsolatedDatabaseId(testName);
        String jdbcUrl = lifecycle.provisionDatabase(dbId);
        Flyway flyway = buildFlyway(jdbcUrl);
        flyway.clean();
        flyway.migrate();
        return new MigrationTestContext(jdbcUrl, dbId, lifecycle);
    }

    @Test
    @DisplayName("V15 should add aligned simulation columns")
    void shouldExposeV15AlignedColumns() throws Exception {
        MigrationTestContext context = createMigratedContext("shouldExposeV15AlignedColumns");
        try (Connection connection = openConnection(context.jdbcUrl())) {
            SimulationMigrationAssertions.assertSchemaReadiness(connection);
        } finally {
            context.cleanup();
        }
    }

    @Test
    @DisplayName("V15 should create simulation_technologies table")
    void shouldCreateSimulationTechnologiesTable() throws Exception {
        MigrationTestContext context = createMigratedContext("shouldCreateSimulationTechnologiesTable");
        try (Connection connection = openConnection(context.jdbcUrl())) {
            SimulationMigrationAssertions.assertSchemaReadiness(connection);
        } finally {
            context.cleanup();
        }
    }

    @Test
    @DisplayName("V15 should create simulation_technologies foreign key to simulations")
    void shouldCreateSimulationTechnologiesForeignKey() throws Exception {
        MigrationTestContext context = createMigratedContext("shouldCreateSimulationTechnologiesForeignKey");
        try (Connection connection = openConnection(context.jdbcUrl())) {
            SimulationMigrationAssertions.assertSchemaReadiness(connection);
        } finally {
            context.cleanup();
        }
    }

    @Test
    @DisplayName("V15 should be registered as applied")
    void shouldRegisterFlywayVersionV15AsApplied() throws Exception {
        MigrationTestContext context = createMigratedContext("shouldRegisterFlywayVersionV15AsApplied");
        try (Connection connection = openConnection(context.jdbcUrl())) {
            SimulationMigrationAssertions.assertFlywayVersionApplied(
                    connection,
                    SimulationMigrationAssertions.EXPECTED_FLYWAY_VERSION);
        } finally {
            context.cleanup();
        }
    }

    private record MigrationTestContext(String jdbcUrl, String databaseId, FallbackDatabaseLifecycle lifecycle) {
        void cleanup() {
            lifecycle.attemptCleanup(databaseId);
        }
    }
}
