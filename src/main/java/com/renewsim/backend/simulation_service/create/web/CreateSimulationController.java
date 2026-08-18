package com.renewsim.backend.simulation_service.create.web;

import com.renewsim.backend.simulation_service.create.application.port.in.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateSimulationFromScenarioUseCase;
import com.renewsim.backend.simulation_service.shared.web.SimulationDetailsWebMapper;
import com.renewsim.backend.simulation_service.create.web.dto.CreateSimulationRequestDTO;
import com.renewsim.backend.simulation_service.create.web.dto.CreateSimulationFromScenarioRequestDTO;
import com.renewsim.backend.simulation_service.shared.web.dto.SimulationDetailsResponseDTO;
import com.renewsim.backend.shared.security.AuthenticatedRequestContext;
import com.renewsim.backend.shared.security.AuthenticatedRequestContextFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
        private final AuthenticatedRequestContextFactory requestContextFactory;

        @Operation(summary = "Create a new simulation", description = "Creates a real simulation from a renewable energy request and runs the matching engine to compute results, financials and a recommendation.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Simulation created", content = @Content(schema = @Schema(implementation = SimulationDetailsResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Missing write scope", content = @Content)
        })
        @PreAuthorize("hasAuthority('SCOPE_write:simulations') or hasRole('ADMIN')")
        @PostMapping
        public ResponseEntity<SimulationDetailsResponseDTO> createSimulation(
                        @Valid @RequestBody CreateSimulationRequestDTO request,
                        Authentication auth) {

                AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);

                SimulationDetailsResponseDTO result = detailsWebMapper.toWebDetails(
                                createRealSimulationUseCase.createSimulation(
                                                createWebMapper.toCommand(request, requestContext.username())));
                log.info("User {} created simulation {}", requestContext.username(), result.id());
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }

        @Operation(summary = "Create a simulation from a predefined scenario", description = "Resolves an active predefined scenario and creates a real simulation from its inputs, reusing the same engine and completion flow as the direct create.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Simulation created from scenario", content = @Content(schema = @Schema(implementation = SimulationDetailsResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Missing write scope", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Scenario not found", content = @Content)
        })
        @PreAuthorize("hasAuthority('SCOPE_write:simulations') or hasRole('ADMIN')")
        @PostMapping("/from-scenario")
        public ResponseEntity<SimulationDetailsResponseDTO> createSimulationFromScenario(
                        @Valid @RequestBody CreateSimulationFromScenarioRequestDTO request,
                        Authentication auth) {

                AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);

                SimulationDetailsResponseDTO result = detailsWebMapper.toWebDetails(
                                createSimulationFromScenarioUseCase.createSimulationFromScenario(
                                                createFromScenarioWebMapper.toCommand(request,
                                                                requestContext.username())));
                log.info("User {} created simulation {} from scenario {}", requestContext.username(), result.id(),
                                request.scenarioId());
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
}
