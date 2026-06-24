package com.renewsim.backend.technology_service.infrastructure.persistence;

import com.renewsim.backend.technology_service.domain.factory.TechnologyFactory;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;
import com.renewsim.backend.technology_service.infrastructure.mapper.TechnologyMapperImpl;
import com.renewsim.backend.technology_service.infrastructure.persistence.adapter.TechnologyRepositoryAdapter;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import com.renewsim.backend.technology_service.infrastructure.persistence.repository.JpaTechnologyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = true)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("testcontainer")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = {
        "com.renewsim.backend.technology_service.infrastructure.persistence.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.renewsim.backend.technology_service.infrastructure.persistence.repository"
})
@Import({TechnologyRepositoryAdapter.class, TechnologyMapperImpl.class})
class TechnologyRepositoryIntegrationTest {

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
    private TechnologyRepositoryAdapter adapter;

    @Autowired
    private JpaTechnologyRepository jpaTechnologyRepository;

    @Test
    @DisplayName("save findById and findByEnergyType should work against MySQL schema")
    void saveFindByIdAndFindByEnergyTypeShouldWorkAgainstMySqlSchema() {
        Technology technology = TechnologyFactory.create(
                "Integration Solar",
                85.0,
                1200,
                100,
                15,
                250,
                18.0,
                "SOLAR");

        Technology saved = adapter.save(technology);

        assertThat(saved.getId()).isNotNull();
        assertThat(adapter.findById(saved.getId())).isPresent();
        assertThat(adapter.findByEnergyType(EnergyType.SOLAR, PageRequest.of(0, 20)).getContent())
                .extracting(Technology::getId)
                .contains(saved.getId());

        TechnologyEntity entity = jpaTechnologyRepository.findById(saved.getId()).orElseThrow();
        assertThat(entity.getEnergyType()).isEqualTo(TechnologyEntity.EnergyType.SOLAR);
    }
}
