package com.renewsim.backend.technology_service.application.port.in;

import com.renewsim.backend.technology_service.application.command.GetTechnologyByIdCommand;
import com.renewsim.backend.technology_service.application.result.TechnologyResponseDTO;
import org.springframework.data.domain.Page;

public interface GetTechnologyUseCase {
    TechnologyResponseDTO getTechnologyById(GetTechnologyByIdCommand command);

    Page<TechnologyResponseDTO> getTechnologies(int page, int size, String energyType, String search, String sortBy, String sortDirection);
}

