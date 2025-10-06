package com.renewsim.backend.technology_service.infrastructure.mapper;

import com.renewsim.backend.technology_service.application.result.*;
import com.renewsim.backend.technology_service.domain.model.Technology;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", implementationName = "TechnologyDtoMapperImpl", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface TechnologyDtoMapper {

    TechnologyQueryResultDTO toQueryResult(Technology domain);

    TechnologyCreationResultDTO toCreationResult(Technology domain);

    TechnologyUpdateResultDTO toUpdateResult(Technology domain);
}
