package com.renewsim.backend.simulation_service.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulationSchemaMigrationIT {

    private final MigrationItModeSelector modeSelector = new MigrationItModeSelector();
    private static final String FALLBACK_POLICY_ERROR = "E_MIGRATION_IT_FALLBACK_POLICY";

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

    private String containerJdbcUrl() {
        return System.getProperty("test.db.url", "jdbc:mysql://localhost:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true");
    }

    private boolean failFastOnFallbackEnabled() {
        return Boolean.parseBoolean(System.getProperty("migration.it.fail-fast-on-fallback", "false"));
    }

    @Test
    @DisplayName("Flyway migrate should prepare simulation schema readiness on isolated DB lifecycle")
    void shouldApplyMigrationChainAndExposeSimulationSchemaReadiness() throws Exception {
        MigrationExecutionContext context = openContext("shouldApplyMigrationChainAndExposeSimulationSchemaReadiness");
        try {
            Flyway flyway = buildFlyway(context.jdbcUrl(), mysqlUser(), mysqlPass());
            flyway.clean();

            MigrateResult result = flyway.migrate();
            assertThat(result.success).isTrue();
            assertThat(result.migrationsExecuted).isGreaterThan(0);

            assertMigrationContract(context.jdbcUrl());
        } finally {
            emitCleanupOutcome(context.close());
        }
    }

    @Test
    @DisplayName("Flyway migrate should be reproducible across isolated clean lifecycles")
    void shouldRemainDeterministicAcrossRepeatedIsolatedRuns() throws Exception {
        MigrationExecutionContext context = openContext("shouldRemainDeterministicAcrossRepeatedIsolatedRuns");
        try {
            Flyway flyway = buildFlyway(context.jdbcUrl(), mysqlUser(), mysqlPass());

            flyway.clean();
            MigrateResult firstRun = flyway.migrate();
            assertThat(firstRun.success).isTrue();
            int firstRunExecuted = firstRun.migrationsExecuted;

            flyway.clean();
            MigrateResult secondRun = flyway.migrate();
            assertThat(secondRun.success).isTrue();
            assertThat(secondRun.migrationsExecuted).isEqualTo(firstRunExecuted);

            assertMigrationContract(context.jdbcUrl());
        } finally {
            emitCleanupOutcome(context.close());
        }
    }

    @Test
    @DisplayName("Container mode should validate the same schema and Flyway contract when available")
    void shouldValidateSharedContractForContainerModeWhenAvailable() throws Exception {
        withPreferredMode("container", () -> {
            MigrationExecutionContext context = openContext("shouldValidateSharedContractForContainerModeWhenAvailable");
            try {
                Assumptions.assumeTrue(
                        context.mode() == MigrationItMode.CONTAINER,
                        "Container runtime unavailable in this environment; parity fallback test still covers shared assertions.");

                Flyway flyway = buildFlyway(context.jdbcUrl(), mysqlUser(), mysqlPass());
                flyway.clean();
                MigrateResult result = flyway.migrate();

                assertThat(result.success).isTrue();
                assertMigrationContract(context.jdbcUrl());
            } finally {
                emitCleanupOutcome(context.close());
            }
            return null;
        });
    }

    @Test
    @DisplayName("Fallback mode should validate the same schema and Flyway contract")
    void shouldValidateSharedContractForFallbackMode() throws Exception {
        withPreferredMode("jdbc-fallback", () -> {
            MigrationExecutionContext context = openContext("shouldValidateSharedContractForFallbackMode");
            try {
                assertThat(context.mode()).isEqualTo(MigrationItMode.JDBC_FALLBACK);

                Flyway flyway = buildFlyway(context.jdbcUrl(), mysqlUser(), mysqlPass());
                flyway.clean();
                MigrateResult result = flyway.migrate();

                assertThat(result.success).isTrue();
                assertMigrationContract(context.jdbcUrl());
            } finally {
                emitCleanupOutcome(context.close());
            }
            return null;
        });
    }

    @Test
    @DisplayName("Flyway migrate should fail when migration chain includes broken script")
    void shouldFailWhenMigrationChainIsBroken() throws Exception {
        MigrationExecutionContext context = openContext("shouldFailWhenMigrationChainIsBroken");
        try {
            Flyway flyway = buildFlywayWithBrokenMigration(context.jdbcUrl(), mysqlUser(), mysqlPass());
            flyway.clean();

            assertThatThrownBy(flyway::migrate)
                    .isInstanceOf(FlywayException.class)
                    .hasMessageContaining("V999");
        } finally {
            emitCleanupOutcome(context.close());
        }
    }

    private MigrationExecutionContext openContext(String testName) throws Exception {
        MigrationItModeSelector.Selection selection = modeSelector.select();
        if (selection.mode() == MigrationItMode.CONTAINER) {
            MigrationExecutionContext context = MigrationExecutionContext.container(
                    containerJdbcUrl(),
                    selection.reasonCode(),
                    selection.probeDetail());
            emitModeEvidence(context);
            return context;
        }

        FallbackDatabaseLifecycle lifecycle = new FallbackDatabaseLifecycle(
                mysqlAdminUrl(),
                mysqlHostPort(),
                mysqlUser(),
                mysqlPass());
        String databaseId = lifecycle.createIsolatedDatabaseId(testName);
        String jdbcUrl = lifecycle.provisionDatabase(databaseId);
        MigrationExecutionContext context = MigrationExecutionContext.fallback(
                jdbcUrl,
                databaseId,
                selection.reasonCode(),
                selection.probeDetail(),
                lifecycle);
        emitModeEvidence(context);
        enforceFallbackPolicy(context);
        return context;
    }

    private void enforceFallbackPolicy(MigrationExecutionContext context) {
        if (failFastOnFallbackEnabled() && context.mode() == MigrationItMode.JDBC_FALLBACK) {
            throw new IllegalStateException(
                    FALLBACK_POLICY_ERROR + ": fallback mode selected, reason=" + context.reasonCode());
        }
    }

    private void emitModeEvidence(MigrationExecutionContext context) {
        MigrationItEvidence.write(context.mode(), context.reasonCode(), context.databaseId());
        System.out.printf(
                "migration-it-mode: mode=%s, reason=%s, isolation-id=%s%n",
                context.mode().name(),
                context.reasonCode(),
                context.databaseId());
    }

    private void emitCleanupOutcome(FallbackDatabaseLifecycle.CleanupStatus cleanupStatus) {
        System.out.printf(
                "migration-it-cleanup: attempted=%s, success=%s, database=%s, detail=%s%n",
                cleanupStatus.attempted(),
                cleanupStatus.success(),
                cleanupStatus.databaseId(),
                cleanupStatus.detail());
    }

    private void assertMigrationContract(String jdbcUrl) throws Exception {
        try (Connection connection = openConnection(jdbcUrl, mysqlUser(), mysqlPass())) {
            SimulationMigrationAssertions.assertSchemaReadiness(connection);
            SimulationMigrationAssertions.assertFlywayVersionApplied(
                    connection,
                    SimulationMigrationAssertions.EXPECTED_FLYWAY_VERSION);
        }
    }

    private <T> T withPreferredMode(String preferredMode, ThrowingSupplier<T> action) throws Exception {
        String previous = System.getProperty(MigrationItModeSelector.PROP_PREFERRED_MODE);
        try {
            System.setProperty(MigrationItModeSelector.PROP_PREFERRED_MODE, preferredMode);
            return action.get();
        } finally {
            if (previous == null) {
                System.clearProperty(MigrationItModeSelector.PROP_PREFERRED_MODE);
            } else {
                System.setProperty(MigrationItModeSelector.PROP_PREFERRED_MODE, previous);
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private record MigrationExecutionContext(
            MigrationItMode mode,
            String jdbcUrl,
            String databaseId,
            String reasonCode,
            String probeDetail,
            FallbackDatabaseLifecycle lifecycle) {

        static MigrationExecutionContext container(String jdbcUrl, String reasonCode, String probeDetail) {
            return new MigrationExecutionContext(MigrationItMode.CONTAINER, jdbcUrl, "n/a", reasonCode, probeDetail, null);
        }

        static MigrationExecutionContext fallback(
                String jdbcUrl,
                String databaseId,
                String reasonCode,
                String probeDetail,
                FallbackDatabaseLifecycle lifecycle) {
            return new MigrationExecutionContext(MigrationItMode.JDBC_FALLBACK, jdbcUrl, databaseId, reasonCode, probeDetail, lifecycle);
        }

        FallbackDatabaseLifecycle.CleanupStatus close() {
            if (mode != MigrationItMode.JDBC_FALLBACK || lifecycle == null) {
                return FallbackDatabaseLifecycle.CleanupStatus.skipped("not_applicable");
            }
            return lifecycle.attemptCleanup(databaseId);
        }
    }

}
