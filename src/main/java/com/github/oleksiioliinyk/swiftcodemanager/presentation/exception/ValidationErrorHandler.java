package com.github.oleksiioliinyk.swiftcodemanager.presentation.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ValidationErrorHandler {
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<Map<String, List<String>>> handleValidation(Exception exception) {
        List<String> messages;

        if (exception instanceof MethodArgumentNotValidException manve) {
            messages = manve.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
        }
        else if (exception instanceof ConstraintViolationException cve) {
            messages = cve.getConstraintViolations()
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
        }
        else {
            messages = List.of(exception.getMessage());
        }

        return ResponseEntity
                .badRequest()
                .body(Map.of("messages", messages));
    }
}
