package com.renewsim.backend.technology_service.infrastructure.mapper;

import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", implementationName = "TechnologyMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TechnologyMapper {

    TechnologyEntity toEntity(Technology domain);

    Technology toDomain(TechnologyEntity entity);
}
