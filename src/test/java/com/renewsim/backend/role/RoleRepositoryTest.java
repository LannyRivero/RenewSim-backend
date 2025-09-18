package com.renewsim.backend.role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.renewsim.backend.role_service.domain.model.RoleName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DisplayName("Integration Test - RoleRepository")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Should find role by name")
    void testShouldFindRoleByName() {

        Role role = new Role(RoleName.ADMIN);
        roleRepository.save(role);

        Optional<Role> foundRole = roleRepository.findByName(RoleName.ADMIN);

        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo(RoleName.ADMIN);
    }

    @Test
    @DisplayName("Should return empty when role not found")
    void testShouldReturnEmptyWhenRoleNotFound() {

        Optional<Role> foundRole = roleRepository.findByName(RoleName.USER);

        assertThat(foundRole).isEmpty();
    }
}
