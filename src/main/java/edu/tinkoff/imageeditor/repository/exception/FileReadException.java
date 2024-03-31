package edu.tinkoff.imageeditor.repository.exception;

public class FileReadException extends Exception {

    public FileReadException() {
    }

    public FileReadException(final String message) {
        super(message);
    }

    public FileReadException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FileReadException(final Throwable cause) {
        super(cause);
    }
}
