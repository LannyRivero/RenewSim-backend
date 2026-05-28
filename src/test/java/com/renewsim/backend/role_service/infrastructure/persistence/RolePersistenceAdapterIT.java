package com.renewsim.backend.role_service.infrastructure.persistence;

import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.shared.domain.vo.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MySQLContainer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
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
@Import({
    com.renewsim.backend.role_service.infrastructure.persistence.adapter.RolePersistenceAdapter.class,
    com.renewsim.backend.role_service.application.mapper.RoleMapperImpl.class
})
class RolePersistenceAdapterIT {

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
    private RoleRepositoryPort roleRepositoryPort;

    @Test
    @DisplayName("RolePersistenceAdapter should retrieve seeded domain Role correctly")
    void saveAndRetrieveRole() {
        Optional<Role> found = roleRepositoryPort.findByName(RoleName.ADMIN);

        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo(RoleName.ADMIN);
    }
}

