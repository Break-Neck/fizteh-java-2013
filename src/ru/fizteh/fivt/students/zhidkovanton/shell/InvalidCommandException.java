package ru.fizteh.fivt.students.zhidkovanton.shell;

public class InvalidCommandException extends Error {
    public InvalidCommandException(final String message) {
        super(message);
    }
}
