package com.renewsim.backend.auth_service.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class StageConfigurationContractTest {

    @Test
    void localYamlSupportsDocumentedJwtSecretVariable() throws IOException {
        String content = Files.readString(Path.of("src/main/resources/application-local.yml"));

        assertThat(content).contains("secret: ${JWT_SECRET:}");
        assertThat(content).contains("secret-base64: ${JWT_SECRET_BASE64:}");
    }

    @Test
    void stageYamlUsesSecurityJwtNamespace() throws IOException {
        String content = Files.readString(Path.of("src/main/resources/application-stage.yml"));

        assertThat(content).contains("security:");
        assertThat(content).contains("jwt:");
        assertThat(content).contains("secret-base64: ${SECURITY_JWT_SECRET_BASE64}");
        assertThat(content).doesNotContain("app:\n  security:");
        assertThat(content).doesNotContain("APP_SECURITY_JWT_");
    }

    @Test
    void productionYamlImportsProdConfiguration() throws IOException {
        String content = Files.readString(Path.of("src/main/resources/application-production.yml"));

        assertThat(content).contains("spring:");
        assertThat(content).contains("config:");
        assertThat(content).contains("import: classpath:application-prod.yml");
    }

    @Test
    void prodYamlTargetsRenewsimFlywaySchemaByDefault() throws IOException {
        String content = Files.readString(Path.of("src/main/resources/application-prod.yml"));

        assertThat(content).contains("schemas: ${FLYWAY_SCHEMA:renewsim}");
    }

    @Test
    void simulationMigrationAlignsAnnualEnergyWithDoubleMapping() throws IOException {
        String content = Files.readString(Path.of("src/main/resources/db/migration/V28__align_simulation_annual_energy_type.sql"));

        assertThat(content).contains("MODIFY COLUMN annual_energy_generated DOUBLE NULL");
    }

    @Test
    void simulationMigrationAlignsRemainingLegacyNumericColumns() throws IOException {
        String content = Files.readString(Path.of("src/main/resources/db/migration/V29__align_simulation_legacy_numeric_columns.sql"));

        assertThat(content).contains("MODIFY COLUMN location_lat DOUBLE NOT NULL");
        assertThat(content).contains("MODIFY COLUMN location_lng DOUBLE NOT NULL");
        assertThat(content).contains("MODIFY COLUMN capacity_kw DOUBLE NOT NULL");
        assertThat(content).contains("MODIFY COLUMN energy_generated DOUBLE NOT NULL DEFAULT 0");
    }

    @Test
    void simulationMigrationAlignsLegacyFinancialNumericColumns() throws IOException {
        String content = Files.readString(Path.of("src/main/resources/db/migration/V30__align_simulation_financial_numeric_columns.sql"));

        assertThat(content).contains("MODIFY COLUMN initial_investment DOUBLE NOT NULL");
        assertThat(content).contains("MODIFY COLUMN total_cost DOUBLE NOT NULL");
    }

    @Test
    void simulationMigrationAlignsProfitabilityColumns() throws IOException {
        String content = Files.readString(Path.of("src/main/resources/db/migration/V31__align_simulation_profitability_numeric_columns.sql"));

        assertThat(content).contains("MODIFY COLUMN npv DOUBLE NULL");
        assertThat(content).contains("MODIFY COLUMN irr_pct DOUBLE NULL");
    }

    @Test
    void stageComposeRequiresExplicitDatabaseSecrets() throws IOException {
        String content = Files.readString(Path.of("docker-compose.stage.yml"));

        assertThat(content).contains("MYSQL_ROOT_PASSWORD: ${STAGE_MYSQL_ROOT_PASSWORD:?STAGE_MYSQL_ROOT_PASSWORD is required}");
        assertThat(content).contains("MYSQL_PASSWORD: ${STAGE_MYSQL_PASSWORD:?STAGE_MYSQL_PASSWORD is required}");
        assertThat(content).doesNotContain("renewsim_root_password");
        assertThat(content).doesNotContain("renewsim_stage_password");
    }

    @Test
    void stageComposeRequiresCriticalOperationalSecrets() throws IOException {
        String content = Files.readString(Path.of("docker-compose.stage.yml"));

        assertThat(content).contains("SECURITY_JWT_SECRET_BASE64: ${SECURITY_JWT_SECRET_BASE64:?SECURITY_JWT_SECRET_BASE64 is required}");
        assertThat(content).contains("OPENWEATHER_API_KEY: ${OPENWEATHER_API_KEY:?OPENWEATHER_API_KEY is required}");
        assertThat(content).contains("APP_FRONTEND_URL: ${APP_FRONTEND_URL:-http://localhost:3000}");
        assertThat(content).doesNotContain("EMAIL_BREVO_API_KEY: ${EMAIL_BREVO_API_KEY:?EMAIL_BREVO_API_KEY is required}");
    }

    @Test
    void loggingEmailAdapterSupportsStageProfile() throws IOException {
        String content = Files.readString(Path.of("src/main/java/com/renewsim/backend/auth_service/infrastructure/email/LoggingEmailAdapter.java"));

        assertThat(content).contains("@Profile({ \"test\", \"local\" })");
        assertThat(content).contains("${app.frontend.url:http://localhost:3000}");
    }

    @Test
    void stageProfileUsesNoopEmailAdapterForShowcase() throws IOException {
        String content = Files.readString(Path.of("src/main/java/com/renewsim/backend/auth_service/infrastructure/email/StageNoopEmailAdapter.java"));

        assertThat(content).contains("@Profile({ \"stage\", \"production\" })");
        assertThat(content).contains("Stage/production adapter");
    }

    @Test
    void authScopesExposeActuatorReadForOperationalRoles() throws IOException {
        String content = Files.readString(Path.of("src/main/resources/application.yml"));

        assertThat(content).contains("ADMIN:");
        assertThat(content).contains("SERVICE_AUTH:");
        assertThat(content).contains("- actuator:read");
    }
}
