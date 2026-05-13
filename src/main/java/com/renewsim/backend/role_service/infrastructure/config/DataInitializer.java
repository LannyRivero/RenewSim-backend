package com.renewsim.backend.role_service.infrastructure.config;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.role_service.infrastructure.persistence.repo.RoleJpaRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleJpaRepository roleRepository;

    @PostConstruct
    public void init() {
        createRoleIfNotExists(RoleName.ADMIN);
        createRoleIfNotExists(RoleName.ANALYST);
        createRoleIfNotExists(RoleName.USER);
    }

     private void createRoleIfNotExists(RoleName roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            RoleEntity role = new RoleEntity();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }
}
