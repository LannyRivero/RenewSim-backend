package com.renewsim.backend.user_service.application.mapper;

import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.web.dto.UserCreateRequest;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import com.renewsim.backend.user_service.infrastructure.persistence.entity.UserEntity;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import com.renewsim.backend.shared.domain.vo.RoleName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", implementationName = "UserServiceMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserServiceMapper {

    // -------- Entity -> Domain --------
    @Mapping(target = "roles", expression = "java(mapRoleEntitiesToRoleNames(entity.getRoles()))")
    User toDomain(UserEntity entity);

    // -------- Domain -> Entity --------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(User domain);

    // -------- Domain -> DTO --------
    UserResponse toResponse(User domain);

    // -------- CreateRequest -> Domain --------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "roles", expression = "java(java.util.Set.of(com.renewsim.backend.shared.domain.vo.RoleName.USER))")
    @Mapping(target = "passwordHash", source = "password") 
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    User toDomain(UserCreateRequest request);

    // -------- Métodos auxiliares para mapear roles --------
    default Set<RoleName> mapRoleEntitiesToRoleNames(Set<RoleEntity> roleEntities) {
        if (roleEntities == null || roleEntities.isEmpty()) {
            return Set.of();
        }
        return roleEntities.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
    }
}
