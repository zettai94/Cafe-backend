package com.indiebiteskch.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<?> handleInsufficientStock(InsufficientStockException ex) {
        // Return a 400 Bad Request with a clear message
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({OrderIDNotFoundException.class, ProductIDNotFoundException.class})
    public ResponseEntity<?> handleNotFound(RuntimeException ex) {
        // Return a 404 Not Found with a clear message
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }
}
