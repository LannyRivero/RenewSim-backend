package com.renewsim.backend.simulation_service.create.web;

import com.renewsim.backend.simulation_service.create.application.port.in.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateSimulationFromScenarioUseCase;
import com.renewsim.backend.simulation_service.shared.web.SimulationRequestContext;
import com.renewsim.backend.simulation_service.shared.web.SimulationDetailsWebMapper;
import com.renewsim.backend.simulation_service.shared.web.SimulationRequestContextFactory;
import com.renewsim.backend.simulation_service.create.web.dto.CreateSimulationFromScenarioRequestDTO;
import com.renewsim.backend.simulation_service.create.web.dto.CreateSolarSimulationRequestDTO;
import com.renewsim.backend.simulation_service.shared.web.dto.SimulationDetailsResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
@Tag(name = "Simulation Create API", description = "Create renewable energy simulations")
public class CreateSimulationController {

    private final CreateRealSimulationUseCase createRealSimulationUseCase;
    private final CreateSimulationFromScenarioUseCase createSimulationFromScenarioUseCase;
    private final CreateSimulationWebMapper createWebMapper = new CreateSimulationWebMapper();
    private final CreateSimulationFromScenarioWebMapper createFromScenarioWebMapper = new CreateSimulationFromScenarioWebMapper();
    private final SimulationDetailsWebMapper detailsWebMapper = new SimulationDetailsWebMapper();
    private final SimulationRequestContextFactory requestContextFactory = new SimulationRequestContextFactory();

    @Operation(summary = "Create a new simulation")
    @PreAuthorize("hasAuthority('SCOPE_write:simulations') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SimulationDetailsResponseDTO> createSimulation(
            @Valid @RequestBody CreateSolarSimulationRequestDTO request,
            Authentication auth) {

        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        SimulationDetailsResponseDTO result = detailsWebMapper.toWebDetails(
                createRealSimulationUseCase.createSimulation(createWebMapper.toCommand(request, requestContext.username())));
        log.info("User {} created simulation {}", requestContext.username(), result.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Create a simulation from a predefined scenario")
    @PreAuthorize("hasAuthority('SCOPE_write:simulations') or hasRole('ADMIN')")
    @PostMapping("/from-scenario")
    public ResponseEntity<SimulationDetailsResponseDTO> createSimulationFromScenario(
            @Valid @RequestBody CreateSimulationFromScenarioRequestDTO request,
            Authentication auth) {

        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        SimulationDetailsResponseDTO result = detailsWebMapper.toWebDetails(
                createSimulationFromScenarioUseCase.createSimulationFromScenario(
                        createFromScenarioWebMapper.toCommand(request, requestContext.username())));
        log.info("User {} created simulation {} from scenario {}", requestContext.username(), result.id(),
                request.scenarioId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
