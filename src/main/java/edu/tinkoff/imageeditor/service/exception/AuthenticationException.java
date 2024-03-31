package edu.tinkoff.imageeditor.service.exception;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(final String message) {
        super(message);
    }

}
