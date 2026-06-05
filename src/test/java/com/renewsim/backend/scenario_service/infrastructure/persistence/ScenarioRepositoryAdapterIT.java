package com.renewsim.backend.scenario_service.infrastructure.persistence;

import com.renewsim.backend.scenario_service.domain.model.Scenario;
import com.renewsim.backend.scenario_service.infrastructure.mapper.ScenarioMapper;
import com.renewsim.backend.scenario_service.infrastructure.persistence.adapter.ScenarioRepositoryAdapter;
import com.renewsim.backend.scenario_service.infrastructure.persistence.entity.ScenarioEntity;
import com.renewsim.backend.scenario_service.infrastructure.persistence.repository.JpaScenarioRepository;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import com.renewsim.backend.shared.domain.vo.Money;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = true)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("testcontainer")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = {
        "com.renewsim.backend.scenario_service.infrastructure.persistence.entity",
        "com.renewsim.backend.technology_service.infrastructure.persistence.entity",
        "com.renewsim.backend.role_service.infrastructure.persistence.entity",
        "com.renewsim.backend.user_service.infrastructure.persistence.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.renewsim.backend.scenario_service.infrastructure.persistence.repository",
        "com.renewsim.backend.technology_service.infrastructure.persistence.repository",
        "com.renewsim.backend.role_service.infrastructure.persistence.repo",
        "com.renewsim.backend.user_service.infrastructure.persistence.repo"
})
@Import({ScenarioRepositoryAdapter.class, ScenarioMapper.class})
class ScenarioRepositoryAdapterIT {

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
    private ScenarioRepositoryAdapter adapter;

    @Autowired
    private JpaScenarioRepository repository;

    @Test
    @DisplayName("should save and read active scenario against migrated MySQL schema")
    void shouldSaveAndReadActiveScenarioAgainstMigratedMySqlSchema() {
        Scenario scenario = new Scenario(
                "Scenario IT",
                "Integration test scenario",
                1L,
                5.0,
                new Money(new BigDecimal("7500.00"), "USD"),
                0.15,
                6000.0,
                new ClimateData(5.5, 3.2, 22.0));

        Scenario saved = adapter.save(scenario);

        assertThat(saved.getId()).isNotNull();
        assertThat(adapter.findById(saved.getId())).isPresent();
        assertThat(adapter.findAllActive()).extracting(Scenario::getId).contains(saved.getId());

        ScenarioEntity entity = repository.findById(saved.getId()).orElseThrow();
        assertThat(entity.getDefaultCapacityKw()).isEqualByComparingTo("5.00");
    }
}
