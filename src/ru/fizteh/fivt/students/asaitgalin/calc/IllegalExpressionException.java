package ru.fizteh.fivt.students.asaitgalin.calc;

public class IllegalExpressionException extends Exception {

    public IllegalExpressionException() {
        super("Input expression contains errors");
    }

    public IllegalExpressionException(String message) {
        super(message);
    }
}
