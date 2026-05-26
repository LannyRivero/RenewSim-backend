package com.renewsim.backend.simulation_service.infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.zip.CRC32;
import java.util.concurrent.atomic.AtomicInteger;

final class FallbackDatabaseLifecycle {

    private static final DateTimeFormatter RUN_ID_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT).withZone(ZoneOffset.UTC);
    private static final int MYSQL_IDENTIFIER_MAX = 64;
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final String adminJdbcUrl;
    private final String dbHostPort;
    private final String user;
    private final String pass;
    private final String runId;

    FallbackDatabaseLifecycle(String adminJdbcUrl, String dbHostPort, String user, String pass) {
        this.adminJdbcUrl = adminJdbcUrl;
        this.dbHostPort = dbHostPort;
        this.user = user;
        this.pass = pass;
        this.runId = RUN_ID_FORMATTER.format(Instant.now());
    }

    String createIsolatedDatabaseId(String testName) {
        String normalizedTestName = testName == null
                ? "unknown"
                : testName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        int sequence = COUNTER.incrementAndGet();
        String prefix = "renewsim_migration_it_" + runId + "_" + sequence + "_";
        String checksum = checksumBase36(normalizedTestName);
        int remaining = MYSQL_IDENTIFIER_MAX - prefix.length() - 1 - checksum.length();
        String suffix = normalizedTestName;
        if (remaining < suffix.length()) {
            suffix = remaining > 0 ? suffix.substring(0, remaining) : "t";
        }
        return prefix + suffix + "_" + checksum;
    }

    private String checksumBase36(String value) {
        CRC32 crc = new CRC32();
        crc.update(value.getBytes(StandardCharsets.US_ASCII));
        return Long.toString(crc.getValue(), 36);
    }

    String provisionDatabase(String databaseId) throws Exception {
        try (Connection connection = DriverManager.getConnection(adminJdbcUrl, user, pass);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE `" + databaseId + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        return buildJdbcUrl(databaseId);
    }

    CleanupStatus attemptCleanup(String databaseId) {
        try (Connection connection = DriverManager.getConnection(adminJdbcUrl, user, pass);
             Statement statement = connection.createStatement()) {
            statement.execute("DROP DATABASE IF EXISTS `" + databaseId + "`");
            return CleanupStatus.success(databaseId);
        } catch (Exception e) {
            return CleanupStatus.failure(databaseId, e.getClass().getSimpleName() + ":" + safeMessage(e.getMessage()));
        }
    }

    private String buildJdbcUrl(String databaseId) {
        return "jdbc:mysql://" + dbHostPort + "/" + databaseId + "?useSSL=false&allowPublicKeyRetrieval=true";
    }

    private String safeMessage(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replace('\n', ' ').replace('\r', ' ').trim();
    }

    record CleanupStatus(String databaseId, boolean attempted, boolean success, String detail) {
        static CleanupStatus success(String databaseId) {
            return new CleanupStatus(databaseId, true, true, "dropped");
        }

        static CleanupStatus failure(String databaseId, String detail) {
            return new CleanupStatus(databaseId, true, false, detail);
        }

        static CleanupStatus skipped(String detail) {
            return new CleanupStatus("n/a", false, true, detail);
        }
    }
}
