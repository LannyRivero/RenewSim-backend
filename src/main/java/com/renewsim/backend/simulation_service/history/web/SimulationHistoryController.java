package com.renewsim.backend.simulation_service.history.web;

import com.renewsim.backend.simulation_service.history.application.port.in.ListUserRealSimulationsUseCase;
import com.renewsim.backend.simulation_service.history.web.dto.ListUserSimulationsResponseDTO;
import com.renewsim.backend.shared.security.AuthenticatedRequestContext;
import com.renewsim.backend.shared.security.AuthenticatedRequestContextFactory;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
public class SimulationHistoryController {

    private final ListUserRealSimulationsUseCase listUserRealSimulationsUseCase;
    private final SimulationHistoryWebMapper historyWebMapper = new SimulationHistoryWebMapper();
    private final AuthenticatedRequestContextFactory requestContextFactory;

    @Operation(summary = "Get authenticated user simulations with pagination")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping({ "/user", "/my-simulations" })
    public ResponseEntity<ListUserSimulationsResponseDTO> getMySimulations(Authentication auth) {
        AuthenticatedRequestContext requestContext = requestContextFactory.from(auth);

        ListUserSimulationsResponseDTO history = historyWebMapper.toWebList(
                listUserRealSimulationsUseCase.getUserSimulations(requestContext.username()));
        log.info("User {} retrieved {} simulations", requestContext.username(), history.total());

        return ResponseEntity.ok(history);
    }
}
