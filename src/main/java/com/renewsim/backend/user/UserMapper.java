package com.renewsim.backend.user;

import com.renewsim.backend.role.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.simulation.SimulationMapper;
import com.renewsim.backend.user.dto.UserRequestDTO;
import com.renewsim.backend.user.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = SimulationMapper.class)
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoleNames")
    UserResponseDTO toResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesFromStrings")
    @Mapping(target = "simulations", ignore = true)
    User toEntity(UserRequestDTO dto);

    @Named("mapRoleNames")
    default Set<String> mapRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }

    @Named("mapRolesFromStrings")
    default Set<Role> mapRolesFromStrings(Set<String> roles) {
        return roles.stream()
                .map(roleName -> new Role(RoleName.valueOf(roleName)))
                .collect(Collectors.toSet());
    }
}
