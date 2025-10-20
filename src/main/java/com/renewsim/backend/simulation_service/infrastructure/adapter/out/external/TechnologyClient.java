package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import com.renewsim.backend.simulation_service.dto.TechnologyResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 🌐 TechnologyClient
 * Feign client for interacting with the Technology Service.
 */
@FeignClient(
    name = "technology-service",
    contextId = "technologyClient",
    url = "${technology-service.url}"
    
)
public interface TechnologyClient {

    @GetMapping("/api/v1/technologies")
    List<TechnologyResponseDTO> getAllTechnologies();

    @GetMapping("/api/v1/technologies/{id}")
    TechnologyResponseDTO getTechnologyById(@PathVariable("id") Long id);
}



