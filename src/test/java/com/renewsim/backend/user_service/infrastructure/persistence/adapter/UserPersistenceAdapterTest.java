package com.renewsim.backend.user_service.infrastructure.persistence.adapter;

import com.renewsim.backend.role_service.infrastructure.persistence.repo.RoleJpaRepository;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.UserAlreadyExistsException;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.mapper.UserServiceMapper;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import com.renewsim.backend.user_service.infrastructure.persistence.entity.UserEntity;
import com.renewsim.backend.user_service.infrastructure.persistence.repo.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private UserJpaRepository repo;

    @Mock
    private UserServiceMapper mapper;

    @Mock
    private RoleJpaRepository roleJpaRepository;

    @InjectMocks
    private UserPersistenceAdapter adapter;

    private static final String VALID_HASH = new BCryptPasswordEncoder(12).encode("pass");

    private User buildUser(Long id, String email) {
        return User.reconstitute(
                id,
                email,
                VALID_HASH,
                "John",
                null,
                UserStatus.ACTIVE,
                Set.of(RoleName.USER),
                LocalDateTime.now(),
                LocalDateTime.now(),
                true,
                LocalDateTime.now());
    }

    @Test
    @DisplayName("save should persist user successfully")
    void saveOk() {
        User domainUser = buildUser(null, "alice@mail.com");
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setEmail("alice@mail.com");

        User savedDomain = buildUser(1L, "alice@mail.com");

        when(mapper.toEntity(any(User.class))).thenReturn(entity);
        when(roleJpaRepository.findByName(RoleName.USER))
                .thenReturn(java.util.Optional.of(
                        new com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity(RoleName.USER)));
        when(repo.save(any(UserEntity.class))).thenReturn(entity);
        when(mapper.toDomain(any(UserEntity.class))).thenReturn(savedDomain);

        User saved = adapter.save(domainUser);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getEmail()).isEqualTo("alice@mail.com");
        verify(repo).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("save should throw UserAlreadyExistsException on duplicate email constraint")
    void saveDuplicate() {
        User domainUser = buildUser(null, "bob@mail.com");
        UserEntity entity = new UserEntity();

        when(mapper.toEntity(any(User.class))).thenReturn(entity);
        when(roleJpaRepository.findByName(RoleName.USER))
                .thenReturn(java.util.Optional.of(
                        new com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity(RoleName.USER)));
        when(repo.save(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException("uk_users_email"));

        assertThatThrownBy(() -> adapter.save(domainUser))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("deleteById should delete user when exists")
    void deleteByIdExists() {
        when(repo.existsById(1L)).thenReturn(true);

        adapter.deleteById(1L);

        verify(repo).existsById(1L);
        verify(repo).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById should throw UserNotFoundException when user does not exist")
    void deleteByIdNotFound() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> adapter.deleteById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verify(repo).existsById(99L);
        verify(repo, never()).deleteById(anyLong());
    }
}