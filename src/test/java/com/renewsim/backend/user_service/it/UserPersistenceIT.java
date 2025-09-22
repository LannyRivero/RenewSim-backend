package com.renewsim.backend.user_service.it;

import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.shared.exception.UserAlreadyExistsException;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.infraestructure.mapper.UserServiceMapper;
import com.renewsim.backend.user_service.infraestructure.persistence.adapter.UserPersistenceAdapter;
import com.renewsim.backend.user_service.infraestructure.persistence.repo.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(showSql = true)
@Testcontainers
@ActiveProfiles("testcontainer")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = "com.renewsim.backend.user_service.infraestructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.renewsim.backend.user_service.infraestructure.persistence.repo")
class UserPersistenceIT {

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
        registry.add("spring.jpa.generate-ddl", () -> "true");
    }

    @Autowired
    private UserPersistenceAdapter persistenceAdapter;

    @Autowired
    private UserJpaRepository repo;

    @BeforeEach
    void cleanDb() {
        repo.deleteAll();
    }

    // ---------------------------
    // Guardar y recuperar
    // ---------------------------
    @Test
    @DisplayName("should save and retrieve user successfully")
    void testSaveAndRetrieveUser() {
        User user = new User(null, "alice", "alice@mail.com", true, Set.of(RoleName.USER), null, null, "StrongPass1");

        User saved = persistenceAdapter.save(user);
        Optional<User> found = persistenceAdapter.findById(saved.id());

        assertThat(found).isPresent();
        assertThat(found.get().username()).isEqualTo("alice");
        assertThat(found.get().email()).isEqualTo("alice@mail.com");
    }

    // ---------------------------
    // Buscar por username
    // ---------------------------
    @Test
    @DisplayName("should find user by username (ignore case)")
    void testFindByUsernameIgnoreCase() {
        persistenceAdapter.save(new User(null, "bob", "bob@mail.com", true, Set.of(RoleName.USER), null, null, "StrongPass1"));

        Optional<User> found = persistenceAdapter.findByUsername("BOB");

        assertThat(found).isPresent();
        assertThat(found.get().email()).isEqualTo("bob@mail.com");
    }

    // ---------------------------
    // Buscar por email
    // ---------------------------
    @Test
    @DisplayName("should find user by email (ignore case)")
    void testFindByEmailIgnoreCase() {
        persistenceAdapter
                .save(new User(null, "charlie", "charlie@mail.com", true, Set.of(RoleName.USER), null, null, "StrongPass1"));

        Optional<User> found = persistenceAdapter.findByEmail("CHARLIE@mail.com");

        assertThat(found).isPresent();
        assertThat(found.get().username()).isEqualTo("charlie");
    }

    // ---------------------------
    // Constraint de unicidad
    // ---------------------------
    @Test
    @DisplayName("should throw exception when saving duplicate email")
    void testDuplicateUserThrowsException() {
        persistenceAdapter
                .save(new User(null, "diana", "diana@mail.com", true, Set.of(RoleName.USER), null, null, "StrongPass1"));

        User duplicate = new User(null, "diana2", "diana@mail.com", true, Set.of(RoleName.USER), null, null, "StrongPass2");

        assertThatThrownBy(() -> persistenceAdapter.save(duplicate))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("should throw exception when saving duplicate username")
    void testDuplicateUsernameThrowsException() {
        persistenceAdapter.save(new User(null, "eric", "eric@mail.com", true, Set.of(RoleName.USER), null, null, "Pass11"));

        User duplicate = new User(null, "eric", "eric2@mail.com", true, Set.of(RoleName.USER), null, null, "Pass22");

        assertThatThrownBy(() -> persistenceAdapter.save(duplicate))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("should set audit fields and enabled by default")
    void testAuditFields() {
        User saved = persistenceAdapter
                .save(new User(null, "frank", "frank@mail.com", true, Set.of(RoleName.USER), null, null, "Pass11"));

        Optional<User> found = persistenceAdapter.findById(saved.id());
        assertThat(found).isPresent();
        assertThat(found.get().enabled()).isTrue();
        assertThat(found.get().createdAt()).isNotNull();
        assertThat(found.get().updatedAt()).isNotNull();
    }

    // ---- Config para importar Adapter ----
    @TestConfiguration
    static class Config {
        @Bean
        UserPersistenceAdapter persistenceAdapter(UserJpaRepository repo, UserServiceMapper mapper) {
            return new UserPersistenceAdapter(repo, mapper);
        }
    }
}
