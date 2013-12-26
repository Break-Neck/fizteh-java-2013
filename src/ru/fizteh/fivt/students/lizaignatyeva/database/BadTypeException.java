package ru.fizteh.fivt.students.lizaignatyeva.database;

public class BadTypeException extends IllegalArgumentException {
    public BadTypeException() {
    }

    public BadTypeException(String message) {
        super(message);
    }

    public BadTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadTypeException(Throwable cause) {
        super(cause);
    }
}
