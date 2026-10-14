package com.renewsim.backend.shared.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.role_service.infrastructure.persistence.repo.RoleJpaRepository;
import com.renewsim.backend.scenario_service.infrastructure.persistence.entity.ScenarioEntity;
import com.renewsim.backend.scenario_service.infrastructure.persistence.repository.JpaScenarioRepository;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import com.renewsim.backend.technology_service.infrastructure.persistence.repository.JpaTechnologyRepository;
import com.renewsim.backend.user_service.infrastructure.persistence.entity.UserEntity;
import com.renewsim.backend.user_service.infrastructure.persistence.repo.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoBootstrapDataRunnerTest {

    @Mock
    private RoleJpaRepository roleRepository;
    @Mock
    private UserJpaRepository userRepository;
    @Mock
    private JpaTechnologyRepository technologyRepository;
    @Mock
    private JpaScenarioRepository scenarioRepository;
    @Mock
    private JpaSimulationRepository simulationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("demo bootstrap seeds simulations with authenticated usernames")
    void runSeedsSimulationsWithAuthenticatedUsernames() {
        DemoBootstrapDataRunner runner = runner();

        stubBootstrapDependencies();
        when(simulationRepository.findByCreatedByOrderByCreatedAtDesc(anyString())).thenReturn(List.of());
        when(simulationRepository.save(any(SimulationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        runner.run(null);

        ArgumentCaptor<SimulationEntity> simulations = ArgumentCaptor.forClass(SimulationEntity.class);
        org.mockito.Mockito.verify(simulationRepository, org.mockito.Mockito.times(5)).save(simulations.capture());

        assertThat(simulations.getAllValues())
                .extracting(SimulationEntity::getCreatedBy)
                .containsExactly(
                        "demo.admin",
                        "demo.admin",
                        "demo.admin",
                        "demo.user",
                        "demo.user");
    }

    @Test
    @DisplayName("demo bootstrap migrates legacy email-owned simulations to authenticated usernames")
    void runMigratesLegacyEmailOwnedSimulationsToAuthenticatedUsernames() {
        DemoBootstrapDataRunner runner = runner();
        SimulationEntity legacyUserSimulation = simulation("Solar legacy Málaga", "demo.user@renewsim.local");

        stubBootstrapDependencies();
        when(simulationRepository.findByCreatedByOrderByCreatedAtDesc(anyString())).thenAnswer(invocation -> {
            String createdBy = invocation.getArgument(0);
            if ("demo.user@renewsim.local".equals(createdBy)) {
                return List.of(legacyUserSimulation);
            }
            return List.of();
        });
        when(simulationRepository.save(any(SimulationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        runner.run(null);

        assertThat(legacyUserSimulation.getCreatedBy()).isEqualTo("demo.user");

        ArgumentCaptor<SimulationEntity> simulations = ArgumentCaptor.forClass(SimulationEntity.class);
        org.mockito.Mockito.verify(simulationRepository, org.mockito.Mockito.times(6)).save(simulations.capture());
        assertThat(simulations.getAllValues()).contains(legacyUserSimulation);
    }

    private DemoBootstrapDataRunner runner() {
        return new DemoBootstrapDataRunner(
                roleRepository,
                userRepository,
                technologyRepository,
                scenarioRepository,
                simulationRepository,
                passwordEncoder,
                new ObjectMapper().findAndRegisterModules());
    }

    private void stubBootstrapDependencies() {
        when(roleRepository.findByName(any(RoleName.class))).thenReturn(Optional.empty());
        when(roleRepository.save(any(RoleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("{bcrypt}demo-hash");
        when(technologyRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(technologyRepository.save(any(TechnologyEntity.class))).thenAnswer(invocation -> {
            TechnologyEntity technology = invocation.getArgument(0);
            technology.setId(10L);
            return technology;
        });
        when(scenarioRepository.findAll()).thenReturn(List.of());
        when(scenarioRepository.save(any(ScenarioEntity.class))).thenAnswer(invocation -> {
            ScenarioEntity scenario = invocation.getArgument(0);
            scenario.setId(20L);
            return scenario;
        });
    }

    private SimulationEntity simulation(String name, String createdBy) {
        SimulationEntity simulation = new SimulationEntity();
        simulation.setName(name);
        simulation.setCreatedBy(createdBy);
        simulation.setStatus("COMPLETED");
        return simulation;
    }
}
