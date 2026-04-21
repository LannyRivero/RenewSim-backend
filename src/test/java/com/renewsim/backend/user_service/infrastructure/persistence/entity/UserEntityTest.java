package com.renewsim.backend.user_service.infrastructure.persistence.entity;

import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    @DisplayName("should handle Set<RoleEntity> correctly")
    void testRolesConversion() {
        UserEntity entity = new UserEntity();

        RoleEntity userRole = new RoleEntity();
        userRole.setId(1L);
        userRole.setName(RoleName.USER);

        RoleEntity adminRole = new RoleEntity();
        adminRole.setId(2L);
        adminRole.setName(RoleName.ADMIN);

        entity.setRoles(Set.of(userRole, adminRole));

        assertThat(entity.getRoles()).hasSize(2);
        assertThat(entity.getRoles()).extracting(RoleEntity::getName)
                .containsExactlyInAnyOrder(RoleName.USER, RoleName.ADMIN);
    }

    @Test
    @DisplayName("should handle empty roles gracefully")
    void testEmptyRoles() {
        UserEntity entity = new UserEntity();
        entity.setRoles(Set.of());
        assertThat(entity.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("equals should return true for entities with same id")
    void testEqualsAndHashCode() {
        UserEntity e1 = new UserEntity();
        e1.setId(1L);
        e1.setUsername("alice");

        UserEntity e2 = new UserEntity();
        e2.setId(1L);
        e2.setUsername("bob");

        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    }

    @Test
    @DisplayName("equals should return false for different ids or null id")
    void testEqualsDifferentIds() {
        UserEntity e1 = new UserEntity();
        e1.setId(1L);

        UserEntity e2 = new UserEntity();
        e2.setId(2L);

        UserEntity e3 = new UserEntity();

        assertThat(e1).isNotEqualTo(e2);
        assertThat(e1).isNotEqualTo(e3);
    }

    @Test
    @DisplayName("toString should contain email and status")
    void testToString() {
        UserEntity entity = new UserEntity();
        entity.setId(42L);
        entity.setUsername("john");
        entity.setEmail("john@example.com");
        entity.setStatus(UserStatus.ACTIVE);

        String str = entity.toString();
        assertThat(str).contains("john@example.com");
        assertThat(str).contains("ACTIVE");
    }

    @Test
    @DisplayName("prePersist should initialize createdAt and updatedAt")
    void testPrePersist() {
        UserEntity entity = new UserEntity();
        entity.prePersist();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("preUpdate should update updatedAt but not createdAt")
    void testPreUpdate() throws InterruptedException {
        UserEntity entity = new UserEntity();
        entity.prePersist();
        Instant createdAt = entity.getCreatedAt();

        Thread.sleep(5);
        entity.preUpdate();

        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }

    @Test
    @DisplayName("default status should be INACTIVE")
    void testDefaultStatus() {
        UserEntity entity = new UserEntity();
        assertThat(entity.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }
}