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
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Arrays;

@RestControllerAdvice
public class AppExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(AppExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleException(final AuthenticationException e) {
        return ResponseEntity.status(403).body(new UiSuccessContainer(false, e.getMessage()));
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<?> handleException(final ConstraintViolationException e) {
        return ResponseEntity.status(400).body(new UiSuccessContainer(false, e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleException(final EntityNotFoundException e) {
        return ResponseEntity.status(404).body(new UiSuccessContainer(false, e.getMessage()));
    }

    @ExceptionHandler(FileReadException.class)
    public ResponseEntity<?> handleException(final FileReadException e) {
        return ResponseEntity.status(400).body(new UiSuccessContainer(false, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(final Exception e) {
        logger.warn(String.format("""
                Caught unprocessed exception %s
                   with message: %s
                   stacktrace: %s""", e.getClass(), e.getMessage(), Arrays.toString(e.getStackTrace())));
        return ResponseEntity.status(500).body(new UiSuccessContainer(false, "Server error"));
    }

}
