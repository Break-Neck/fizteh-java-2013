package ru.fizteh.fivt.robot;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 */
public abstract class RobotLeg {

    private final RobotLegType type;
    private final OutputStream output;

    protected RobotLeg(RobotLegType type, OutputStream output) {
        this.type = type;
        this.output = output;
    }

    public final RobotLegType getType() {
        return type;
    }

    /**
     * Выполняет шаг. Реализация должна вызывать метод {@link #makeStep()}.
     *
     * @return true, если робот может продолжать ходить.
     */
    public abstract boolean step();

    protected final void makeStep() {
        try {
            output.write((type + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("IO error occurred", e);
        }
    }
}
