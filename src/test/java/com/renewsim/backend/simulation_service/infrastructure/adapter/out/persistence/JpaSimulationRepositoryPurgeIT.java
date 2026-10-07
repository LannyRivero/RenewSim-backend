package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = true)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("testcontainer")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackageClasses = SimulationEntity.class)
@EnableJpaRepositories(basePackageClasses = JpaSimulationRepository.class)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class JpaSimulationRepositoryPurgeIT {

    private static final long USER_ID = 902_310L;
    private static final long OLD_DELETED_SIMULATION_ID = 902_311L;
    private static final long RECENT_DELETED_SIMULATION_ID = 902_312L;
    private static final long OLD_COMPLETED_SIMULATION_ID = 902_313L;
    private static final long OLD_DELETED_CHAT_SESSION_ID = 902_314L;
    private static final long RECENT_DELETED_CHAT_SESSION_ID = 902_315L;

    @Container
    @SuppressWarnings("resource")
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("renewsim")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private JpaSimulationRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("deleteDeletedOlderThan should purge only expired DELETED simulations on migrated MySQL schema")
    void deleteDeletedOlderThanPurgesOnlyExpiredDeletedRowsAndAppliesRelationshipActions() {
        insertUser();
        insertSimulation(OLD_DELETED_SIMULATION_ID, "Expired deleted simulation", "DELETED", "2026-08-01 10:00:00");
        insertSimulation(RECENT_DELETED_SIMULATION_ID, "Recent deleted simulation", "DELETED", "2026-09-20 10:00:00");
        insertSimulation(OLD_COMPLETED_SIMULATION_ID, "Old completed simulation", "COMPLETED", "2026-08-01 10:00:00");
        insertRelationships();

        int deleted = repository.deleteDeletedOlderThan(LocalDateTime.parse("2026-09-01T00:00:00"));

        assertThat(deleted).isEqualTo(1);
        assertThat(simulationExists(OLD_DELETED_SIMULATION_ID)).isFalse();
        assertThat(simulationExists(RECENT_DELETED_SIMULATION_ID)).isTrue();
        assertThat(simulationExists(OLD_COMPLETED_SIMULATION_ID)).isTrue();
        assertThat(countRows("simulation_technologies", "simulation_id", OLD_DELETED_SIMULATION_ID)).isZero();
        assertThat(countRows("simulation_share_tokens", "simulation_id", OLD_DELETED_SIMULATION_ID)).isZero();
        assertThat(countRows("simulation_technologies", "simulation_id", RECENT_DELETED_SIMULATION_ID)).isEqualTo(1);
        assertThat(countRows("simulation_share_tokens", "simulation_id", RECENT_DELETED_SIMULATION_ID)).isEqualTo(1);
        assertThat(chatSessionSimulationId(OLD_DELETED_CHAT_SESSION_ID)).isNull();
        assertThat(chatSessionSimulationId(RECENT_DELETED_CHAT_SESSION_ID)).isEqualTo(RECENT_DELETED_SIMULATION_ID);
    }

    private void insertUser() {
        jdbc.update("""
                INSERT INTO users (id, username, email, password, enabled, account_non_expired, account_non_locked, credentials_non_expired)
                VALUES (?, 'purge-it-user', 'purge-it-user@renewsim.local', 'encoded-password', true, true, true, true)
                """, USER_ID);
    }

    private void insertSimulation(long id, String name, String status, String updatedAt) {
        jdbc.update("""
                INSERT INTO simulations (
                    id, name, location_name, location_lat, location_lng, capacity_kw,
                    simulation_years, discount_rate, initial_investment, annual_maintenance_cost,
                    total_cost, energy_generated, co2_reduction, status, created_at, updated_at,
                    location, energy_type, project_size, budget, estimated_energy, created_by
                ) VALUES (
                    ?, ?, 'Sevilla', 37.3891, -5.9845, 300.0,
                    25, 5.00, 315000.0, 7200.0,
                    322200.0, 457200.0, 125000.0, ?, '2026-07-01 10:00:00', ?,
                    'Sevilla, Spain', 'solar', 300.0, 315000.0, 457200.0, 'purge-it-user'
                )
                """, id, name, status, updatedAt);
    }

    private void insertRelationships() {
        jdbc.update("INSERT INTO simulation_technologies (simulation_id, technology_id) VALUES (?, 1001)", OLD_DELETED_SIMULATION_ID);
        jdbc.update("INSERT INTO simulation_technologies (simulation_id, technology_id) VALUES (?, 1002)", RECENT_DELETED_SIMULATION_ID);
        jdbc.update("""
                INSERT INTO simulation_share_tokens (simulation_id, token, created_by, expires_at)
                VALUES (?, 'expired-deleted-token', ?, '2026-12-31 00:00:00')
                """, OLD_DELETED_SIMULATION_ID, USER_ID);
        jdbc.update("""
                INSERT INTO simulation_share_tokens (simulation_id, token, created_by, expires_at)
                VALUES (?, 'recent-deleted-token', ?, '2026-12-31 00:00:00')
                """, RECENT_DELETED_SIMULATION_ID, USER_ID);
        jdbc.update("""
                INSERT INTO chat_sessions (id, session_id, user_id, simulation_id, title)
                VALUES (?, 'expired-deleted-session', ?, ?, 'Expired deleted session')
                """, OLD_DELETED_CHAT_SESSION_ID, USER_ID, OLD_DELETED_SIMULATION_ID);
        jdbc.update("""
                INSERT INTO chat_sessions (id, session_id, user_id, simulation_id, title)
                VALUES (?, 'recent-deleted-session', ?, ?, 'Recent deleted session')
                """, RECENT_DELETED_CHAT_SESSION_ID, USER_ID, RECENT_DELETED_SIMULATION_ID);
        jdbc.update("""
                INSERT INTO chat_messages (session_id, role, content)
                VALUES (?, 'USER', 'Will survive with the chat session')
                """, OLD_DELETED_CHAT_SESSION_ID);
    }

    private boolean simulationExists(long id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM simulations WHERE id = ?", Integer.class, id);
        return count != null && count == 1;
    }

    private int countRows(String table, String column, long value) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?", Integer.class, value);
        return count == null ? 0 : count;
    }

    private Long chatSessionSimulationId(long chatSessionId) {
        return jdbc.queryForObject("SELECT simulation_id FROM chat_sessions WHERE id = ?", Long.class, chatSessionId);
    }
}
