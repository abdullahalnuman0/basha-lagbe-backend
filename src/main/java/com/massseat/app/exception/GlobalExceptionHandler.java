package com.massseat.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<?> handleGeneric(Exception e, HttpServletRequest req) {
        log.error("Unhandled exception", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", req, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), req, null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException e, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, e.getMessage(), req, null);
    }

    @ExceptionHandler(AccountStateException.class)
    public ResponseEntity<ErrorResponse> handleAccountLifecycle(AccountStateException e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                        ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                                .message(e.getMessage())
                                .path(req.getRequestURI())
                                .code(e.getCode())
                                .build()
                );
    }

    // --- Error message builder ---
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req, Map<String, String> fieldErrors) {
        return ResponseEntity.status(status)
                .body(
                        ErrorResponse.builder()
                                .status(status.value())
                                .message(message)
                                .error(status.getReasonPhrase())
                                .path(req.getRequestURI())
                                .fieldErrors(fieldErrors)
                                .build()
                );
    }

}
