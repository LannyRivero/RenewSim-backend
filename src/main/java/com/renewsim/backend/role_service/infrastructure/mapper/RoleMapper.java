package com.renewsim.backend.role_service.infrastructure.mapper;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    implementationName = "RoleMapperImpl",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RoleMapper {

    // Entity ↔ Domain
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    Role toDomain(RoleEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    RoleEntity toEntity(Role domain);

    // Helpers
    default RoleName toRoleName(RoleEntity entity) {
        return entity != null ? entity.getName() : null;
    }

    default RoleEntity toRoleEntity(RoleName roleName) {
        if (roleName == null) return null;
        RoleEntity entity = new RoleEntity();
        entity.setName(roleName);
        return entity;
    }
}
