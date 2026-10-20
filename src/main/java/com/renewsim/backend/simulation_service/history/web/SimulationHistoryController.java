package com.renewsim.backend.simulation_service.history.web;

import com.renewsim.backend.simulation_service.history.application.port.in.ListUserRealSimulationsUseCase;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPageRequest;
import com.renewsim.backend.simulation_service.history.web.dto.ListUserSimulationsResponseDTO;
import com.renewsim.backend.shared.security.AuthenticatedRequestContext;
import com.renewsim.backend.shared.security.AuthenticatedRequestContextFactory;
import com.renewsim.backend.shared.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


@Slf4j
@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
@Tag(name = "Simulation History API", description = "List the simulations of the authenticated user")
public class SimulationHistoryController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ListUserRealSimulationsUseCase listUserRealSimulationsUseCase;
    private final SimulationHistoryWebMapper historyWebMapper = new SimulationHistoryWebMapper();
    private final AuthenticatedRequestContextFactory requestContextFactory;

    @Operation(summary = "Get authenticated user simulations", description = "Returns a page of the authenticated user's simulations. Optional location filtering ignores case and diacritics. Supports sorting by createdAt, name, technology, location, or status.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation list retrieved", content = @Content(schema = @Schema(implementation = ListUserSimulationsResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping({ "/user", "/my-simulations" })
    public ResponseEntity<ListUserSimulationsResponseDTO> getMySimulations(
            Authentication auth,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);
        if (page < 0) {
            throw new BadRequestException("Page index must not be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (location != null && location.length() > 255) {
            throw new BadRequestException("Location filter must not exceed 255 characters");
        }
        SimulationHistoryPageRequest.SortDirection sortDirection;
        try {
            sortDirection = SimulationHistoryPageRequest.SortDirection.valueOf(direction.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Sort direction must be 'asc' or 'desc'");
        }
        SimulationHistoryPageRequest.SortField sortField = switch (sortBy) {
            case "createdAt" -> SimulationHistoryPageRequest.SortField.CREATED_AT;
            case "name" -> SimulationHistoryPageRequest.SortField.NAME;
            case "technology" -> SimulationHistoryPageRequest.SortField.TECHNOLOGY;
            case "location" -> SimulationHistoryPageRequest.SortField.LOCATION;
            case "status" -> SimulationHistoryPageRequest.SortField.STATUS;
            default -> throw new BadRequestException("Unsupported sort field");
        };

        ListUserSimulationsResponseDTO history = historyWebMapper.toWebList(
                listUserRealSimulationsUseCase.getUserSimulations(
                        requestContext.username(), location,
                        new SimulationHistoryPageRequest(page, size, sortField, sortDirection)));
        log.info("User {} retrieved {} simulations", requestContext.username(), history.total());

        return ResponseEntity.ok(history);
    }
}
