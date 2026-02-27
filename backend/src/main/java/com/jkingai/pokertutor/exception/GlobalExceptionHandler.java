package com.jkingai.pokertutor.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Global exception handler for consistent error responses across all controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleGameNotFound(GameNotFoundException ex) {
        // TODO: Return 404 with error body:
        //   { "error": { "code": "GAME_NOT_FOUND", "message": "...", "details": {} } }
        throw new UnsupportedOperationException("TODO: Implement handleGameNotFound");
    }

    @ExceptionHandler(InvalidActionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAction(InvalidActionException ex) {
        // TODO: Return 400 with error body:
        //   { "error": { "code": "INVALID_ACTION", "message": "...", "details": { "validActions": [...] } } }
        throw new UnsupportedOperationException("TODO: Implement handleInvalidAction");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        // TODO: Return 500 with error body:
        //   { "error": { "code": "INTERNAL_ERROR", "message": "...", "details": {} } }
        throw new UnsupportedOperationException("TODO: Implement handleGenericError");
    }
}
