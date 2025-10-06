package com.renewsim.backend.technology_service.application.port.in;

import java.util.List;

import com.renewsim.backend.technology_service.application.command.GetTechnologyByIdCommand;
import com.renewsim.backend.technology_service.application.result.TechnologyQueryResultDTO;

public interface GetTechnologyUseCase {
    TechnologyQueryResultDTO getTechnologyById(GetTechnologyByIdCommand command);
    
    List<TechnologyQueryResultDTO> getAllTechnologies();
}

