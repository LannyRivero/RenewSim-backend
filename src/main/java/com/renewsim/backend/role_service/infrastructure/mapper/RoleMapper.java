package com.renewsim.backend.role_service.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;

@Mapper(componentModel = "spring", implementationName = "RoleMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    // Entity ↔ Domain
    @Mapping(target = "name", source = "name")
    Role toDomain(RoleEntity entity);

    @Mapping(target = "name", source = "name")
    RoleEntity toEntity(Role domain);

}
