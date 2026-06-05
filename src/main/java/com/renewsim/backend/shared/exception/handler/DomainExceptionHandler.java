package com.renewsim.backend.shared.exception.handler;

import com.renewsim.backend.role_service.domain.exception.LastAdminRemovalException;
import com.renewsim.backend.scenario_service.domain.exception.ScenarioNotFoundException;
import com.renewsim.backend.shared.dto.ErrorResponse;
import com.renewsim.backend.shared.exception.*;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.technology_service.domain.exception.DuplicateTechnologyNameException;
import com.renewsim.backend.technology_service.domain.exception.TechnologyNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Handles domain/business logic exceptions (HTTP 404, 409).
 * Consolidates entity not found and resource conflict scenarios.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class DomainExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler({
            EntityNotFoundException.class,
            UserNotFoundException.class,
            RoleNotFoundException.class,
            ScenarioNotFoundException.class,
            TechnologyNotFoundException.class,
            SimulationNotFoundException.class,
            ResourceNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return buildError(
                HttpStatus.NOT_FOUND,
                "ENTITY_NOT_FOUND",
                "Entity not found",
                ex.getMessage(),
                req,
                null);
    }

    @ExceptionHandler({
            ConflictException.class,
            UserAlreadyExistsException.class,
            RoleAlreadyExistsException.class,
            DuplicateTechnologyNameException.class,
            LastAdminRemovalException.class,
            ResourceConflictException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return buildError(
                HttpStatus.CONFLICT,
                "RESOURCE_CONFLICT",
                "Resource conflict",
                ex.getMessage(),
                req,
                null);
    }
}
