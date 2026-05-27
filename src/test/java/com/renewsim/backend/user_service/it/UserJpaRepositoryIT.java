package com.renewsim.backend.user_service.it;

import com.renewsim.backend.user_service.infrastructure.persistence.entity.UserEntity;
import com.renewsim.backend.user_service.infrastructure.persistence.repo.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = true)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = "com.renewsim.backend.user_service.infraestructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.renewsim.backend.user_service.infraestructure.persistence.repo")
class UserJpaRepositoryIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("renewsim_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private UserJpaRepository repo;

    @BeforeEach
    void cleanDb() {
        repo.deleteAll();
    }

    private UserEntity createUser(String username, String email, boolean enabled) {
        UserEntity e = new UserEntity();
        e.setUsername(username);
        e.setEmail(email);
        e.setPasswordHash("pw12345");
        e.setStatus(enabled
                ? com.renewsim.backend.user_service.domain.model.UserStatus.ACTIVE
                : com.renewsim.backend.user_service.domain.model.UserStatus.INACTIVE);
        return repo.save(e);
    }

    // ---------------------------
    // findAllSummaries
    // ---------------------------
    @Test
    @DisplayName("should return summaries with only id, username and email")
    void testFindAllSummaries() {
        createUser("alice", "alice@mail.com", true);
        createUser("bob", "bob@mail.com", true);

        List<UserJpaRepository.UserSummary> summaries = repo.findAllSummaries();

        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(0).getId()).isNotNull();
        assertThat(summaries.get(0).getUsername()).isNotBlank();
        assertThat(summaries.get(0).getEmail()).contains("@");
    }

    // ---------------------------
    // searchSummaries
    // ---------------------------
    @Test
    @DisplayName("should filter summaries by username fragment")
    void testSearchSummariesByUsername() {
        createUser("charlie", "charlie@mail.com", true);
        createUser("charlotte", "charlotte@mail.com", true);
        createUser("david", "david@mail.com", true);

        Page<UserJpaRepository.UserSummary> page = repo.searchSummaries(
                "char", null, true, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(UserJpaRepository.UserSummary::getUsername)
                .contains("charlie", "charlotte");
    }

    @Test
    @DisplayName("should filter summaries by email fragment")
    void testSearchSummariesByEmail() {
        createUser("eric", "eric@mail.com", true);
        createUser("eva", "eva@test.com", true);

        Page<UserJpaRepository.UserSummary> page = repo.searchSummaries(
                null, "mail.com", true, PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(UserJpaRepository.UserSummary::getEmail)
                .allMatch(email -> email.endsWith("mail.com"));
    }
}
