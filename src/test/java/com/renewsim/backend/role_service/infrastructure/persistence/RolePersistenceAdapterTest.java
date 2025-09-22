package com.renewsim.backend.role_service.infrastructure.persistence;

import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.role_service.infrastructure.persistence.repo.RoleJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest(showSql = true)
@Testcontainers
@ActiveProfiles("testcontainer")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = {
    "com.renewsim.backend.role_service.infrastructure.persistence.entity",
    "com.renewsim.backend.user_service.infraestructure.persistence.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.renewsim.backend.role_service.infrastructure.persistence.repo",
    "com.renewsim.backend.user_service.infraestructure.persistence.repo"
})
class RolePersistenceAdapterTest {

    @Container
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
    private RoleJpaRepository roleJpaRepository;

    @Test
    @DisplayName("save and find role should work with MySQL Testcontainer")
    void saveAndFindRole() {
        RoleEntity entity = new RoleEntity(RoleName.ADMIN);
        RoleEntity saved = roleJpaRepository.save(entity);

        Optional<RoleEntity> found = roleJpaRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(RoleName.ADMIN);
    }
}

