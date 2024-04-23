package edu.tinkoff.imageeditorapi.web.security.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenException extends AuthenticationException {

    public InvalidTokenException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public InvalidTokenException(final String msg) {
        super(msg);
    }
}
