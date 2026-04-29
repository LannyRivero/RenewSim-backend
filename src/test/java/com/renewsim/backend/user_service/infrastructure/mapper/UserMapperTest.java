package com.renewsim.backend.user_service.infrastructure.mapper;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.user_service.application.mapper.UserServiceMapper;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import com.renewsim.backend.user_service.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class UserMapperTest {

    private final UserServiceMapper mapper = Mappers.getMapper(UserServiceMapper.class);
    private static final String VALID_HASH = new BCryptPasswordEncoder(12).encode("StrongPass1");

    @Test
    @DisplayName("should map UserEntity to User domain")
    void testMapUserEntityToDomain() {
        RoleEntity userRole = new RoleEntity(RoleName.USER);
        RoleEntity adminRole = new RoleEntity(RoleName.ADMIN);

        UserEntity entity = new UserEntity();
        entity.setId(10L);
        entity.setUsername("charlie");
        entity.setEmail("charlie@mail.com");
        entity.setPasswordHash(VALID_HASH);
        entity.setStatus(UserStatus.ACTIVE);
        entity.setRoles(Set.of(userRole, adminRole));

        User user = mapper.toDomain(entity);

        assertThat(user.getId()).isEqualTo(10L);
        assertThat(user.getEmail()).isEqualTo("charlie@mail.com");
        assertThat(user.getRoles()).containsExactlyInAnyOrder(RoleName.USER, RoleName.ADMIN);
        assertThat(user.getPasswordHash()).isEqualTo(VALID_HASH);
    }

    @Test
    @DisplayName("should map User domain to UserResponse DTO")
    void testMapUserToUserResponse() {
        User user = User.reconstitute(
                1L,
                "john@example.com",
                VALID_HASH,
                "John",
                null,
                UserStatus.ACTIVE,
                Set.of(RoleName.USER),
                LocalDateTime.now(),
                LocalDateTime.now(),
                true,
                LocalDateTime.now());

        UserResponse dto = mapper.toResponse(user);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.email()).isEqualTo("alice@mail.com");
        assertThat(dto.roles()).containsExactlyInAnyOrder("USER", "ADMIN");
    }
}