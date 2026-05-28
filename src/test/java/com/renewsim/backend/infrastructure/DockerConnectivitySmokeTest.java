package com.renewsim.backend.infrastructure;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

class DockerConnectivitySmokeTest {

    @Test
    void testcontainersCanStartMysql() {
        try (MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")) {
            mysql.start();
            assertThat(mysql.isRunning()).isTrue();
            assertThat(mysql.getJdbcUrl()).contains("jdbc:mysql://");
        }
    }
}
