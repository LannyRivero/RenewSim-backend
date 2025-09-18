package com.renewsim.backend.role;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.renewsim.backend.role_service.domain.model.RoleName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role getRoleByName(RoleName roleName) {
        return roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol " + roleName + " no existe"));
    }

    @Override
    public Set<Role> getRolesFromStrings(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            try {
                RoleName enumName = RoleName.valueOf(roleName.toUpperCase());
                Role role = getRoleByName(enumName);
                roles.add(role);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido: " + roleName);
            }
        }
        return roles;
    }

    @Override
    public Set<Role> getRolesByNames(List<String> roleNames) {
        return roleNames.stream()
                .map(name -> roleRepository.findByName(RoleName.valueOf(name))
                        .orElseThrow(() -> new RuntimeException("Role not found: " + name)))
                .collect(Collectors.toSet());
    }
}

