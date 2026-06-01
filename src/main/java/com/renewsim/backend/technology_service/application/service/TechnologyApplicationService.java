package com.renewsim.backend.technology_service.application.service;


import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.technology_service.application.command.CreateTechnologyCommand;
import com.renewsim.backend.technology_service.application.command.DeleteTechnologyCommand;
import com.renewsim.backend.technology_service.application.command.GetTechnologyByIdCommand;
import com.renewsim.backend.technology_service.application.command.UpdateTechnologyCommand;
import com.renewsim.backend.technology_service.application.port.in.CreateTechnologyUseCase;
import com.renewsim.backend.technology_service.application.port.in.DeleteTechnologyUseCase;
import com.renewsim.backend.technology_service.application.port.in.GetTechnologyUseCase;
import com.renewsim.backend.technology_service.application.port.in.UpdateTechnologyUseCase;
import com.renewsim.backend.technology_service.application.result.TechnologyCreationResultDTO;
import com.renewsim.backend.technology_service.application.result.TechnologyQueryResultDTO;
import com.renewsim.backend.technology_service.application.result.TechnologyUpdateResultDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TechnologyApplicationService implements
        CreateTechnologyUseCase,
        UpdateTechnologyUseCase,
        DeleteTechnologyUseCase,
        GetTechnologyUseCase {

    private final TechnologyCommandService commandService;

    @Override
    @CacheEvict(value = "technologies", allEntries = true)
    public TechnologyCreationResultDTO createTechnology(CreateTechnologyCommand command) {
        return commandService.handleCreate(command);
    }

    @Override
    @CacheEvict(value = "technologies", allEntries = true)
    public TechnologyUpdateResultDTO updateTechnology(UpdateTechnologyCommand command) {
        return commandService.handleUpdate(command);
    }

    @Override
    public void deleteTechnology(DeleteTechnologyCommand command) {
        commandService.handleDelete(command);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "technologies", key = "'id:' + #command.id()")
    public TechnologyQueryResultDTO getTechnologyById(GetTechnologyByIdCommand command) {
        return commandService.handleGetById(command);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "technologies", key = "'all'")
    public List<TechnologyQueryResultDTO> getAllTechnologies() {
        return commandService.handleGetAll();
    }
}

