package com.renewsim.backend.user_service.infraestructure.mapper;

import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;
import com.renewsim.backend.role_service.domain.model.RoleName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    implementationName = "UserServiceMapperImpl",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserServiceMapper {

    // -------- Entity -> Domain --------
    @Mapping(target = "roles", expression = "java(mapStringsToRoles(entity.getRoles()))")
    User toDomain(UserEntity entity);

    // -------- Domain -> Entity --------
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(domain.roles()))")
    UserEntity toEntity(User domain);

    // -------- Domain -> DTO --------
    UserResponse toResponse(User domain);

    // -------- CreateRequest -> Domain --------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "roles", expression = "java(java.util.Set.of(com.renewsim.backend.role_service.domain.model.RoleName.USER))")
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    User toDomain(UserCreateRequest request);

    // -------- Métodos auxiliares para mapear roles --------
    default Set<String> mapRolesToStrings(Set<RoleName> roles) {
        return roles == null ? Set.of()
                : roles.stream().map(Enum::name).collect(Collectors.toSet());
    }

    default Set<RoleName> mapStringsToRoles(Set<String> roles) {
        return roles == null ? Set.of()
                : roles.stream()
                       .map(r -> RoleName.valueOf(r.toUpperCase()))
                       .collect(Collectors.toSet());
    }
}
