package com.renewsim.backend.shared.exception.handler;

import com.renewsim.backend.shared.dto.ErrorResponse;
import com.renewsim.backend.shared.exception.BadRequestException;
import com.renewsim.backend.shared.exception.InvalidUserDataException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles validation and bad request errors (HTTP 400).
 * Includes field-level validation errors, type mismatches, and malformed requests.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidationExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage())
        );
        return buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Validation error",
                "Invalid request body",
                req,
                fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv ->
                fieldErrors.put(cv.getPropertyPath().toString(), cv.getMessage())
        );
        return buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Validation error",
                "Invalid request parameter",
                req,
                fieldErrors
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage())
        );
        return buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Validation error",
                "Invalid request parameter",
                req,
                fieldErrors
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerValidation(HandlerMethodValidationException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getAllValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(err ->
                        fieldErrors.put(result.getMethodParameter().getParameterName(), err.getDefaultMessage()))
        );
        return buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Validation error",
                "Invalid method argument",
                req,
                fieldErrors
        );
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            ConversionFailedException.class,
            TypeMismatchException.class,
            MissingPathVariableException.class,
            NumberFormatException.class,
            InvalidUserDataException.class,
            BadRequestException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest req) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Bad request",
                ex.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "Malformed request",
                "Request body is not readable or malformed JSON",
                req,
                null
        );
    }
}
