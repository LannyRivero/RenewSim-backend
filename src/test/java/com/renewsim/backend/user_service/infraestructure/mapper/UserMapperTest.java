package com.renewsim.backend.user_service.infraestructure.mapper;

import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class UserMapperTest {
    private final UserServiceMapper mapper = Mappers.getMapper(UserServiceMapper.class);

    @Test
    @DisplayName("should map User domain to UserResponse DTO")
    void testMapUserToUserResponse() {
        User user = new User(
                1L,
                "alice",
                "alice@mail.com",
                true,
                Set.of(RoleName.USER, RoleName.ADMIN),
                Instant.now(),
                Instant.now(),
                "hashedPass");

        UserResponse dto = mapper.toResponse(user);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.username()).isEqualTo("alice");
        assertThat(dto.email()).isEqualTo("alice@mail.com");
        assertThat(dto.enabled()).isTrue();
        assertThat(dto.roles()).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    @DisplayName("should map UserCreateRequest DTO to User domain with default USER role")
    void testMapUserCreateRequestToUser() {
        UserCreateRequest request = new UserCreateRequest("bob", "bob@mail.com", "StrongPass1");

        User user = mapper.toDomain(request);

        assertThat(user).isNotNull();
        assertThat(user.id()).isNull();
        assertThat(user.username()).isEqualTo("bob");
        assertThat(user.email()).isEqualTo("bob@mail.com");
        assertThat(user.roles()).containsExactly(RoleName.USER);
        assertThat(user.passwordHash()).isEqualTo("StrongPass1");
    }

    @Test
    @DisplayName("should map UserEntity to User domain and rolesCsv to Set")
    void testMapUserEntityToDomain() {
        UserEntity entity = new UserEntity();
        entity.setId(10L);
        entity.setUsername("charlie");
        entity.setEmail("charlie@mail.com");
        RoleEntity userRole = new RoleEntity();
        userRole.setName(RoleName.USER);
        RoleEntity adminRole = new RoleEntity();
        adminRole.setName(RoleName.ADMIN);
        entity.setRoles(Set.of(userRole, adminRole));
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setPasswordHash("hashed");

        User user = mapper.toDomain(entity);

        assertThat(user.id()).isEqualTo(10L);
        assertThat(user.username()).isEqualTo("charlie");
        assertThat(user.roles()).containsExactlyInAnyOrder(RoleName.USER, RoleName.ADMIN);
    } 

    
}
