package ru.fizteh.fivt.students.vyatkina.database.logging;


import java.util.concurrent.atomic.AtomicBoolean;

public class CloseState {

    private AtomicBoolean isClosed = new AtomicBoolean(false);
    private final String standardCloseMessage;

    public CloseState() {
        standardCloseMessage = "The object is closed";
    }

    public CloseState(String standardCloseMessage) {
        this.standardCloseMessage = standardCloseMessage;
    }

    public void close() {
        isClosed.set(true);
    }

    public boolean isAlreadyClosed() {
        return isClosed.get();
    }

    public void isClosedCheck() {
        if (isClosed.get()) {
            throw new IllegalStateException(standardCloseMessage);
        }
    }

    public void isClosedCheck(String message) {
        if (isClosed.get()) {
            throw new IllegalStateException(message);
        }
    }

}
