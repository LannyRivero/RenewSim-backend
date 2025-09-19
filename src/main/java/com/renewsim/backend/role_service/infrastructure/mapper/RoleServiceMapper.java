package com.renewsim.backend.role_service.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.dto.RoleDTO;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;

@Mapper(componentModel = "spring", implementationName = "RoleServiceMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleServiceMapper {

    Role toDomain(RoleEntity entity);

    RoleEntity toEntity(Role domain);

    RoleDTO toDTO(Role domain);

    Role toDomain(RoleDTO dto);

    default RoleName toRoleName(RoleEntity entity) {
        return entity.getName();

    }

    default RoleEntity toRoleEntity(RoleName roleName) {
        RoleEntity entity = new RoleEntity();
        entity.setName(roleName);
        return entity;

    }

}
