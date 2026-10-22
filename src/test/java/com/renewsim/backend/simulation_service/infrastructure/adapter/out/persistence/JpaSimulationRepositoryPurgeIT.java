package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.create.infrastructure.adapter.out.persistence.CreateSimulationRepositoryAdapter;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationCompletion;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationResultRevisionEntity;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        private SimulationResultRevisionJpaRepository revisionRepository;

        @Autowired
        private JdbcTemplate jdbc;

        @Autowired
        private PlatformTransactionManager transactionManager;

        @Test
        @DisplayName("deleteDeletedOlderThan should purge only expired DELETED simulations on migrated MySQL schema")
        void deleteDeletedOlderThanPurgesOnlyExpiredDeletedRowsAndAppliesRelationshipActions() {
                insertUser();
                insertSimulation(OLD_DELETED_SIMULATION_ID, "Expired deleted simulation", "DELETED",
                                "2026-08-01 10:00:00");
                insertSimulation(RECENT_DELETED_SIMULATION_ID, "Recent deleted simulation", "DELETED",
                                "2026-09-20 10:00:00");
                insertSimulation(OLD_COMPLETED_SIMULATION_ID, "Old completed simulation", "COMPLETED",
                                "2026-08-01 10:00:00");
                insertRevision(OLD_DELETED_SIMULATION_ID, 1, "old-input", "old-result");
                insertRelationships();

                int deleted = repository.deleteDeletedOlderThan(LocalDateTime.parse("2026-09-01T00:00:00"));

                assertThat(deleted).isEqualTo(1);
                assertThat(simulationExists(OLD_DELETED_SIMULATION_ID)).isFalse();
                assertThat(simulationExists(RECENT_DELETED_SIMULATION_ID)).isTrue();
                assertThat(simulationExists(OLD_COMPLETED_SIMULATION_ID)).isTrue();
                assertThat(countRows("simulation_technologies", "simulation_id", OLD_DELETED_SIMULATION_ID)).isZero();
                assertThat(countRows("simulation_result_revisions", "simulation_id", OLD_DELETED_SIMULATION_ID))
                                .isZero();
                assertThat(countRows("simulation_share_tokens", "simulation_id", OLD_DELETED_SIMULATION_ID)).isZero();
                assertThat(countRows("simulation_technologies", "simulation_id", RECENT_DELETED_SIMULATION_ID))
                                .isEqualTo(1);
                assertThat(countRows("simulation_share_tokens", "simulation_id", RECENT_DELETED_SIMULATION_ID))
                                .isEqualTo(1);
                assertThat(chatSessionSimulationId(OLD_DELETED_CHAT_SESSION_ID)).isNull();
                assertThat(chatSessionSimulationId(RECENT_DELETED_CHAT_SESSION_ID))
                                .isEqualTo(RECENT_DELETED_SIMULATION_ID);
        }

        @Test
        @DisplayName("detail read mapping prefers the current revision and falls back to legacy snapshots")
        void resultReadUsesCurrentRevisionAndKeepsLegacySnapshotFallback() {
                insertUser();
                insertSimulation(902_340L, "Revision-backed simulation", "COMPLETED", "2026-08-01 10:00:00");
                jdbc.update("UPDATE simulations SET result_snapshot = 'legacy-result' WHERE id = ?", 902_340L);
                insertRevision(902_340L, 1, "revision-input", "revision-result");
                Long revisionId = jdbc.queryForObject(
                                "SELECT id FROM simulation_result_revisions WHERE simulation_id = ?", Long.class,
                                902_340L);
                jdbc.update("UPDATE simulations SET current_revision_id = ? WHERE id = ?", revisionId, 902_340L);

                SimulationRecordEntityMapper mapper = new SimulationRecordEntityMapper(
                                new ObjectMapper().findAndRegisterModules());
                SimulationReadModel revisionBacked = mapper.toReadModel(repository.findById(902_340L).orElseThrow());

                assertThat(revisionBacked.resultSnapshot()).isEqualTo("revision-result");

                insertSimulation(902_341L, "Legacy simulation", "COMPLETED", "2026-08-01 10:00:00");
                jdbc.update("UPDATE simulations SET result_snapshot = 'legacy-only-result' WHERE id = ?", 902_341L);
                SimulationReadModel legacy = mapper.toReadModel(repository.findById(902_341L).orElseThrow());

                assertThat(legacy.resultSnapshot()).isEqualTo("legacy-only-result");
        }

        @Test
        @DisplayName("completed executions append revisions and keep the pointer on the newest snapshot")
        void completedExecutionsAppendRevisionsWithoutChangingPreviousSnapshots() {
                insertUser();
                SimulationRecordEntityMapper mapper = new SimulationRecordEntityMapper(
                                new ObjectMapper().findAndRegisterModules());
                CreateSimulationRepositoryAdapter adapter = new CreateSimulationRepositoryAdapter(
                                repository, mapper, revisionRepository);
                Simulation simulation = draftSimulation();

                adapter.save(simulation);
                simulation.complete(completion("first-run-result", 1_000.0));
                adapter.save(simulation);
                simulation.update(completion("second-run-result", 1_200.0), LocalDateTime.parse("2026-08-02T10:00:00"));
                adapter.save(simulation);

                List<SimulationResultRevisionEntity> revisions = revisionRepository.findAll().stream()
                                .filter(revision -> revision.getSimulationId().equals(simulation.getId().value()))
                                .sorted(java.util.Comparator
                                                .comparing(SimulationResultRevisionEntity::getRevisionNumber))
                                .toList();
                assertThat(revisions).hasSize(2);
                assertThat(revisions).extracting(SimulationResultRevisionEntity::getRevisionNumber).containsExactly(1,
                                2);
                assertThat(revisions).extracting(SimulationResultRevisionEntity::getResultSnapshot)
                                .containsExactly("first-run-result", "second-run-result");

                SimulationEntity latest = repository.findById(simulation.getId().value()).orElseThrow();
                assertThat(latest.getCurrentRevisionId()).isEqualTo(revisions.get(1).getId());
                assertThat(mapper.toReadModel(latest).resultSnapshot()).isEqualTo("second-run-result");
        }

        @Test
        @DisplayName("a failed enclosing transaction rolls back both the simulation and its result revision")
        void failedCompletionTransactionDoesNotLeaveSimulationOrRevisionRows() {
                new TransactionTemplate(transactionManager).executeWithoutResult(status -> insertUser());
                SimulationRecordEntityMapper mapper = new SimulationRecordEntityMapper(
                                new ObjectMapper().findAndRegisterModules());
                CreateSimulationRepositoryAdapter adapter = new CreateSimulationRepositoryAdapter(
                                repository, mapper, revisionRepository);
                Simulation simulation = draftSimulation();
                TransactionTemplate transaction = new TransactionTemplate(transactionManager);
                transaction.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);

                assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
                        adapter.save(simulation);
                        simulation.complete(completion("rolled-back-result", 1_000.0));
                        adapter.save(simulation);
                        throw new IllegalStateException("force rollback after revision persistence");
                })).isInstanceOf(IllegalStateException.class)
                                .hasMessage("force rollback after revision persistence");

                Integer simulationCount = jdbc.queryForObject(
                                "SELECT COUNT(*) FROM simulations WHERE created_by = ?", Integer.class,
                                simulation.getCreatedBy());
                assertThat(simulationCount).isZero();
                assertThat(countRows("simulation_result_revisions", "simulation_id", simulation.getId().value()))
                                .isZero();
        }

        @Test
        @DisplayName("searchHistory applies a case-insensitive name filter before pagination")
        void searchHistoryFiltersNamesBeforePaginating() {
                insertUser();
                for (int index = 0; index < 10; index++) {
                        insertSimulation(902_320L + index, "Other simulation " + index, "COMPLETED",
                                        "2026-08-01 10:00:00");
                }
                insertSimulation(902_330L, "Solar hidden record", "COMPLETED", "2026-08-01 10:00:00");

                var result = repository.searchHistory("purge-it-user", "solar hidden", null, PageRequest.of(0, 10));

                assertThat(result.getTotalElements()).isEqualTo(1);
                assertThat(result.getContent()).extracting(SimulationEntity::getName)
                                .containsExactly("Solar hidden record");
        }

        private void insertUser() {
                jdbc.update("""
                                INSERT IGNORE INTO users (id, username, email, password, enabled, account_non_expired, account_non_locked, credentials_non_expired)
                                VALUES (?, 'purge-it-user', 'purge-it-user@renewsim.local', 'encoded-password', true, true, true, true)
                                """,
                                USER_ID);
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
                jdbc.update("INSERT INTO simulation_technologies (simulation_id, technology_id) VALUES (?, 1001)",
                                OLD_DELETED_SIMULATION_ID);
                jdbc.update("INSERT INTO simulation_technologies (simulation_id, technology_id) VALUES (?, 1002)",
                                RECENT_DELETED_SIMULATION_ID);
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

        private void insertRevision(long simulationId, int revisionNumber, String inputSnapshot,
                        String resultSnapshot) {
                jdbc.update("""
                                INSERT INTO simulation_result_revisions (
                                    simulation_id, revision_number, input_snapshot, result_snapshot, created_at
                                ) VALUES (?, ?, ?, ?, '2026-08-01 10:00:00')
                                """, simulationId, revisionNumber, inputSnapshot, resultSnapshot);
        }

        private Simulation draftSimulation() {
                return Simulation.create(
                                "Revision persistence simulation",
                                Technology.solar(),
                                SimulationLocation.of("Sevilla, Spain", 37.3891, -5.9845, "Spain",
                                                CountryCode.of("ES")),
                                new SimulationSystem(5.0, 0.81, 0.5, 99.0,
                                                new SimulationSystem.LossesPct(2.0, 6.0, 1.0, 3.0, 1.0)),
                                ConsumptionProfile.of(12_000.0, List.of(1_000.0, 1_000.0, 1_000.0, 1_000.0,
                                                1_000.0, 1_000.0, 1_000.0, 1_000.0, 1_000.0, 1_000.0, 1_000.0,
                                                1_000.0)),
                                new SimulationEconomics(Currency.of("EUR"), 10_000.0, 100.0, 0.18, 0.07, 8,
                                                ProjectLifetime.of(20)),
                                List.of(), null, "purge-it-user");
        }

        private SimulationCompletion completion(String resultSnapshot, double generation) {
                return new SimulationCompletion(resultSnapshot, generation, 100.0, 500.0, 8.5, "viable", List.of());
        }

        private boolean simulationExists(long id) {
                Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM simulations WHERE id = ?", Integer.class, id);
                return count != null && count == 1;
        }

        private int countRows(String table, String column, long value) {
                Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?",
                                Integer.class, value);
                return count == null ? 0 : count;
        }

        private Long chatSessionSimulationId(long chatSessionId) {
                return jdbc.queryForObject("SELECT simulation_id FROM chat_sessions WHERE id = ?", Long.class,
                                chatSessionId);
        }
}
