package com.renewsim.backend.role_service.application.mapper;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.web.dto.RoleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", implementationName = "RoleDtoMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleDtoMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "createdAt", source = "createdAt")
    RoleDTO toDTO(Role domain);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "createdAt", source = "createdAt")
    Role toDomain(RoleDTO dto);
}
