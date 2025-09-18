package com.renewsim.backend.user_service.infraestructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.infrastructure.persistence.RoleEntity;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    @DisplayName("should convert roles Set <-> CSV correctly")
    void testRolesConversion() {
        UserEntity entity = new UserEntity();

        RoleEntity userRole = new RoleEntity();
        userRole.setName(RoleName.USER);
        RoleEntity adminRole = new RoleEntity();
        adminRole.setName(RoleName.ADMIN);
        entity.setRoles(Set.of(userRole, adminRole));
        Set<RoleEntity> roles = entity.getRoles();
        assertThat(roles).hasSize(2);
        assertThat(roles).extracting("name").containsExactlyInAnyOrder(RoleName.USER, RoleName.ADMIN);

        assertThat(roles).extracting("name").containsExactlyInAnyOrder(RoleName.USER, RoleName.ADMIN);
    }

    @Test
    @DisplayName("should handle empty rolesCsv gracefully")
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

        UserEntity e3 = new UserEntity(); // id null

        assertThat(e1).isNotEqualTo(e2);
        assertThat(e1).isNotEqualTo(e3);
    }

    @Test
    @DisplayName("toString should contain username and email")
    void testToString() {
        UserEntity entity = new UserEntity();
        entity.setId(42L);
        entity.setUsername("john");
        entity.setEmail("john@example.com");
        entity.setEnabled(true);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        
        RoleEntity userRole = new RoleEntity();
        userRole.setName(RoleName.USER);
        entity.setRoles(Set.of(userRole));

        String str = entity.toString();

        assertThat(str).contains("john");
        assertThat(str).contains("john@example.com");
        assertThat(str).contains("USER");
    }

    @Test
    @DisplayName("prePersist should initialize createdAt, updatedAt and enabled=true")
    void testPrePersist() {
        UserEntity entity = new UserEntity();
        entity.prePersist();
        assertThat(entity.isEnabled()).isTrue();
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
        assertThat(entity.getUpdatedAt()).isAfter(createdAt);
    }

    @Test
    @DisplayName("equals should return false when comparing with different class")
    void testEqualsDifferentClass() {
        UserEntity e1 = new UserEntity();
        e1.setId(1L);
        assertThat(e1.equals("string")).isFalse();
    }

}
