     package com.renewsim.backend.role_service.application.mapper;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.dto.RoleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", implementationName = "RoleDtoMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    RoleDTO toDTO(Role domain);

    @Mapping(target = "name", source = "name")
    Role toDomain(RoleDTO dto);
}
