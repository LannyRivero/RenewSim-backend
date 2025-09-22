package com.renewsim.backend.role_service.infrastructure.persistence.adapter;

import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.role_service.infrastructure.persistence.repo.RoleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RolePersistenceAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository roleJpaRepository;

    public RolePersistenceAdapter(RoleJpaRepository roleJpaRepository) {
        this.roleJpaRepository = roleJpaRepository;
    }

    @Override
    public Optional<Role> findById(Long roleId) {
        return roleJpaRepository.findById(roleId)
                .map(entity -> new Role(entity.getId(), entity.getName()));
    }

    @Override
    public Optional<Role> findByName(RoleName roleName) {
        return roleJpaRepository.findByName(roleName)
                .map(entity -> new Role(entity.getId(), entity.getName()));
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = new RoleEntity();
        entity.setId(role.id());
        entity.setName(role.name());
        RoleEntity saved = roleJpaRepository.save(entity);
        return new Role(saved.getId(), saved.getName());
    }

    @Override
    public List<Role> findAll() {
        return roleJpaRepository.findAll()
                .stream()
                .map(entity -> new Role(entity.getId(), entity.getName()))
                .toList();
    }

    @Override
    public void deleteById(Long roleId) {
        roleJpaRepository.deleteById(roleId);
    }
}
