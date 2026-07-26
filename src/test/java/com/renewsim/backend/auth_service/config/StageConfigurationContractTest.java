package com.renewsim.backend.auth_service.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class StageConfigurationContractTest {

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
    void stageComposeRequiresExplicitDatabaseSecrets() throws IOException {
        String content = Files.readString(Path.of("docker-compose.stage.yml"));

        assertThat(content).contains("MYSQL_ROOT_PASSWORD: ${STAGE_MYSQL_ROOT_PASSWORD}");
        assertThat(content).contains("MYSQL_PASSWORD: ${STAGE_MYSQL_PASSWORD}");
        assertThat(content).doesNotContain("renewsim_root_password");
        assertThat(content).doesNotContain("renewsim_stage_password");
    }
}
