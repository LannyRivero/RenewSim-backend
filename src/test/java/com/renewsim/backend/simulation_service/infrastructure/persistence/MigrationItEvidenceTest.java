package com.renewsim.backend.simulation_service.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationItEvidenceTest {

    @Test
    @DisplayName("should write migration IT evidence JSON with expected keys")
    void shouldWriteEvidencePayload() throws Exception {
        MigrationItEvidence.write(MigrationItMode.JDBC_FALLBACK, "container_unavailable", "db_123");

        assertThat(MigrationItEvidence.EVIDENCE_PATH).exists();

        String payload = Files.readString(MigrationItEvidence.EVIDENCE_PATH);
        assertThat(payload).contains("\"mode\": \"JDBC_FALLBACK\"");
        assertThat(payload).contains("\"fallbackReason\": \"container_unavailable\"");
        assertThat(payload).contains("\"isolationId\": \"db_123\"");
    }
}
