package com.renewsim.backend.role_service.infrastructure.persistence.adapter;

import com.renewsim.backend.role_service.application.mapper.RoleMapper;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.role_service.infrastructure.persistence.repo.RoleJpaRepository;
import com.renewsim.backend.shared.domain.vo.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolePersistenceAdapterCacheTest {

    @Mock
    private RoleJpaRepository roleJpaRepository;

    @Mock
    private RoleMapper mapper;

    private RolePersistenceAdapter rolePersistenceAdapter;

    @BeforeEach
    void setUp() {
        rolePersistenceAdapter = new RolePersistenceAdapter(roleJpaRepository, mapper);
    }

    @Test
    @DisplayName("findById should map repository entity to domain")
    void findById_shouldMapEntityToDomain() {
        RoleEntity entity = new RoleEntity(1L, RoleName.ADMIN, "Administrator", LocalDateTime.now());
        Role domain = new Role(1L, RoleName.ADMIN, "Administrator", LocalDateTime.now());
        when(roleJpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<Role> result = rolePersistenceAdapter.findById(1L);

        assertThat(result).contains(domain);
        verify(roleJpaRepository).findById(1L);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("findByName should map repository entity to domain")
    void findByName_shouldMapEntityToDomain() {
        RoleEntity entity = new RoleEntity(2L, RoleName.USER, "User", LocalDateTime.now());
        Role domain = new Role(2L, RoleName.USER, "User", LocalDateTime.now());
        when(roleJpaRepository.findByName(RoleName.USER)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<Role> result = rolePersistenceAdapter.findByName(RoleName.USER);

        assertThat(result).contains(domain);
        verify(roleJpaRepository).findByName(RoleName.USER);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("save should map domain to entity and back")
    void save_shouldMapDomainEntityAndBack() {
        Role domain = new Role(RoleName.ANALYST);
        RoleEntity toSave = new RoleEntity(null, RoleName.ANALYST, null, LocalDateTime.now());
        RoleEntity saved = new RoleEntity(3L, RoleName.ANALYST, null, LocalDateTime.now());
        Role mappedBack = new Role(3L, RoleName.ANALYST, null, LocalDateTime.now());

        when(mapper.toEntity(domain)).thenReturn(toSave);
        when(roleJpaRepository.save(toSave)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(mappedBack);

        Role result = rolePersistenceAdapter.save(domain);

        assertThat(result).isEqualTo(mappedBack);
        verify(mapper).toEntity(domain);
        verify(roleJpaRepository).save(toSave);
        verify(mapper).toDomain(saved);
    }

    @Test
    @DisplayName("findAll should map all entities to domain list")
    void findAll_shouldMapAllEntitiesToDomain() {
        RoleEntity entity = new RoleEntity(1L, RoleName.ADMIN, "Administrator", LocalDateTime.now());
        Role domain = new Role(1L, RoleName.ADMIN, "Administrator", LocalDateTime.now());
        when(roleJpaRepository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        List<Role> result = rolePersistenceAdapter.findAll();

        assertThat(result).containsExactly(domain);
        verify(roleJpaRepository).findAll();
        verify(mapper).toDomain(entity);
    }
}
