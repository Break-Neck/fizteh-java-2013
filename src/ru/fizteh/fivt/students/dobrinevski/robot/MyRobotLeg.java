package ru.fizteh.fivt.students.dobrinevski.robot;

import ru.fizteh.fivt.robot.RobotLeg;
import ru.fizteh.fivt.robot.RobotLegType;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

public class MyRobotLeg extends RobotLeg {
    private AtomicBoolean turn;
    private Integer step;
    private final Lock lock;

    public MyRobotLeg(RobotLegType type, OutputStream output, AtomicBoolean myTurn, int stepCount, Lock locker) {
        super(type, output);
        turn = myTurn;
        step = stepCount;
        lock = locker;
    }

    public boolean step() {
        if (step == 0) {
            return false;
        }
        if (getType() == RobotLegType.RIGHT && !turn.get() && lock.tryLock()) {
            try {
                makeStep();
                step--;
                turn.set(!turn.get());
                return true;
            } finally {
                lock.unlock();
            }
        }
        if (getType() == RobotLegType.LEFT && turn.get() && lock.tryLock()) {
            try {
                makeStep();
                step--;
                turn.set(!turn.get());
                return true;
            } finally {
                lock.unlock();
            }
        }
        return true;
    }
}
