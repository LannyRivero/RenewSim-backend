package com.renewsim.backend.user_service.infraestructure.persistence.adapter;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.UserAlreadyExistsException;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.infraestructure.mapper.UserServiceMapper;
import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;
import com.renewsim.backend.user_service.infraestructure.persistence.repo.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private UserJpaRepository repo;

    @Mock
    private UserServiceMapper mapper; 

    @InjectMocks
    private UserPersistenceAdapter adapter;

    @Test
    @DisplayName("save should persist user successfully")
    void saveOk() {
        User domainUser = new User(null, "alice", "alice@mail.com", true, Set.of(RoleName.USER), null, null, "secret");
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setUsername("alice");
        entity.setEmail("alice@mail.com");

        when(mapper.toEntity(any(User.class))).thenReturn(entity);
        when(repo.save(any(UserEntity.class))).thenReturn(entity);
        when(mapper.toDomain(any(UserEntity.class))).thenReturn(
                new User(1L, "alice", "alice@mail.com", true, Set.of(RoleName.USER), null, null, "secret"));

        User saved = adapter.save(domainUser);

        assertThat(saved.id()).isEqualTo(1L);
        assertThat(saved.username()).isEqualTo("alice");
        assertThat(saved.email()).isEqualTo("alice@mail.com");

        verify(repo, times(1)).save(any(UserEntity.class));
        verify(mapper, times(1)).toEntity(any(User.class));
        verify(mapper, times(1)).toDomain(any(UserEntity.class));
    }

    @Test
    @DisplayName("save should throw UserAlreadyExistsException on duplicate constraint")
    void saveDuplicate() {
        User domainUser = new User(null, "bob", "bob@mail.com", true, Set.of(RoleName.USER), null, null, "secret");

        when(mapper.toEntity(any(User.class))).thenReturn(new UserEntity());
        when(repo.save(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry uk_user_email"));

        assertThatThrownBy(() -> adapter.save(domainUser))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("bob@mail.com");

        verify(repo, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("deleteById should delete user when exists")
    void deleteByIdExists() {
        when(repo.existsById(1L)).thenReturn(true);

        adapter.deleteById(1L);

        verify(repo, times(1)).existsById(1L);
        verify(repo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById should throw UserNotFoundException when user does not exist")
    void deleteByIdNotFound() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> adapter.deleteById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("id=99");

        verify(repo, times(1)).existsById(99L);
        verify(repo, never()).deleteById(anyLong());
    }
}
