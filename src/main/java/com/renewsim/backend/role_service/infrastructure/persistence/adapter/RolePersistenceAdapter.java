package com.renewsim.backend.role_service.infrastructure.persistence.adapter;

import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.infrastructure.mapper.RoleServiceMapper;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.role_service.infrastructure.persistence.repo.RoleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RolePersistenceAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository roleJpaRepository;
    private final RoleServiceMapper mapper;

    public RolePersistenceAdapter(RoleJpaRepository roleJpaRepository, RoleServiceMapper mapper) {
        this.roleJpaRepository = roleJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Role> findById(Long roleId) {
        return roleJpaRepository.findById(roleId).map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(RoleName roleName) {
        return roleJpaRepository.findByName(roleName).map(mapper::toDomain);
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = mapper.toEntity(role);
        RoleEntity saved = roleJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Role> findAll() {
        return roleJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long roleId) {
        roleJpaRepository.deleteById(roleId);
    }
}
