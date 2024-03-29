package edu.tinkoff.imageeditor.web.exception;

import edu.tinkoff.imageeditor.dto.UiSuccessContainer;
import edu.tinkoff.imageeditor.repository.exception.FileReadException;
import edu.tinkoff.imageeditor.service.exception.AuthenticationException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
public class AppExceptionHandler {

    final Logger logger = LoggerFactory.getLogger(AppExceptionHandler.class);

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
        logger.warn(String.format("""
                Caught unprocessed exception %s
                   with message: %s
                   stacktrace: %s""", e.getClass(), e.getMessage(), Arrays.toString(e.getStackTrace())));
        return ResponseEntity.status(500).body(new UiSuccessContainer(false, "Server error"));
    }

}
