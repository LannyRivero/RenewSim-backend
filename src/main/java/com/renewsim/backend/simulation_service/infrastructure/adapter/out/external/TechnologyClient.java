package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import com.renewsim.backend.simulation_service.dto.TechnologyResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "technology-service", url = "${services.technology.url}")
public interface TechnologyClient {

    @GetMapping("/api/v1/technologies")
    List<TechnologyResponseDTO> getAllTechnologies();

    @GetMapping("/api/v1/technologies/{id}")
    TechnologyResponseDTO getTechnologyById(@PathVariable Long id);
}

