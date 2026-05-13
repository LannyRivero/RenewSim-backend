package com.renewsim.backend.role_service.application.mapper;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", implementationName = "RoleMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    // Entity ↔ Domain
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "createdAt", source = "createdAt")
    Role toDomain(RoleEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "createdAt", source = "createdAt")
    RoleEntity toEntity(Role domain);

}
