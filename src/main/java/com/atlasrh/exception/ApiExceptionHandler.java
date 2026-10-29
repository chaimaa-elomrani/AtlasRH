package com.atlasrh.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException ex) { return response(HttpStatus.NOT_FOUND, ex.getMessage()); }

    @ExceptionHandler({BusinessConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<Map<String, Object>> conflict(RuntimeException ex) {
        String message = ex instanceof BusinessConflictException ? ex.getMessage() : "The operation conflicts with existing data";
        return response(HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler({InvalidRequestException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> badRequest(Exception ex) { return response(HttpStatus.BAD_REQUEST, ex.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        Map<String, Object> body = body(HttpStatus.BAD_REQUEST, "Request validation failed");
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) { return ResponseEntity.status(status).body(body(status, message)); }
    private Map<String, Object> body(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now()); body.put("status", status.value()); body.put("error", status.getReasonPhrase()); body.put("message", message);
        return body;
    }
}
