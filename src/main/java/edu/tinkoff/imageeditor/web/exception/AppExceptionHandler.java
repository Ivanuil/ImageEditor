package edu.tinkoff.imageeditor.web.exception;

import edu.tinkoff.imageeditor.dto.UiSuccessContainer;
import edu.tinkoff.imageeditor.repository.exception.FileReadException;
import edu.tinkoff.imageeditor.service.exception.AuthenticationException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleException(AuthenticationException e) {
        return ResponseEntity.status(403).body(new UiSuccessContainer(false, e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleException(ConstraintViolationException e) {
        return ResponseEntity.status(400).body(new UiSuccessContainer(false, e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleException(EntityNotFoundException e) {
        return ResponseEntity.status(404).body(new UiSuccessContainer(false, e.getMessage()));
    }

    @ExceptionHandler(FileReadException.class)
    public ResponseEntity<?> handleException(FileReadException e) {
        return ResponseEntity.status(400).body(new UiSuccessContainer(false, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.status(500).body(new UiSuccessContainer(false, "Server error"));
    }

}
