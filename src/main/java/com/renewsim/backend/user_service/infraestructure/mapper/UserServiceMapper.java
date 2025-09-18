package com.renewsim.backend.user_service.infraestructure.mapper;

import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.infrastructure.persistence.RoleEntity;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;

@Mapper(
    componentModel = "spring",
    implementationName = "UserServiceMapperImpl",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserServiceMapper {

    // -------- Entity -> Domain --------
    @Mapping(target = "roles", expression = "java(toDomainRoles(entity.getRoles()))")
    User toDomain(UserEntity entity);

    // -------- Domain -> Entity --------
    @Mapping(target = "roles", expression = "java(toEntityRoles(domain.getRoles()))")
    UserEntity toEntity(User domain);

    // -------- Domain -> DTO --------
    UserResponse toResponse(User domain);

    // -------- CreateRequest -> Domain --------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "roles", expression = "java(java.util.Set.of(com.renewsim.backend.role_service.domain.model.RoleName.USER))")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    User toDomain(UserCreateRequest request);

    // -------- Helpers --------
    default Set<RoleName> toDomainRoles(Set<RoleEntity> entities) {
        if (entities == null) return Set.of();
        return entities.stream()
                .map(RoleEntity::getName) // RoleEntity → RoleName
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    default Set<RoleEntity> toEntityRoles(Set<RoleName> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(roleName -> {
                    RoleEntity entity = new RoleEntity();
                    entity.setName(roleName);
                    return entity;
                })
                .collect(java.util.stream.Collectors.toSet());
    }
}
