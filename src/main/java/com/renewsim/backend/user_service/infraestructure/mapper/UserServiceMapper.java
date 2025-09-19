package com.renewsim.backend.user_service.infraestructure.mapper;

import com.renewsim.backend.role_service.infrastructure.mapper.RoleServiceMapper;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infraestructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = RoleServiceMapper.class, implementationName = "UserServiceMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserServiceMapper {

    // -------- Entity -> Domain --------
    User toDomain(UserEntity entity);

    // -------- Domain -> Entity --------
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
}
