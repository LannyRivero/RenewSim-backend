package com.renewsim.backend.role_service.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.dto.RoleDTO;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;

@Mapper(
    componentModel = "spring",
    implementationName = "RoleServiceMapperImpl",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RoleServiceMapper {

    // -------- Entity ↔ Domain --------
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    Role toDomain(RoleEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    RoleEntity toEntity(Role domain);

    // -------- Domain ↔ DTO --------
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    RoleDTO toDTO(Role domain);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    Role toDomain(RoleDTO dto);

    // -------- Entity ↔ RoleName --------
    // Estos métodos permiten que MapStruct pueda convertir
    // automáticamente colecciones Set<RoleEntity> ↔ Set<RoleName>
    default RoleName toRoleName(RoleEntity entity) {
        return entity != null ? entity.getName() : null;
    }

    default RoleEntity toRoleEntity(RoleName roleName) {
        if (roleName == null) {
            return null;
        }
        RoleEntity entity = new RoleEntity();
        entity.setName(roleName);
        return entity;
    }
}


