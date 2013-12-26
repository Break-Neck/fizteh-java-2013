package ru.fizteh.fivt.students.dobrinevski.robot;

import ru.fizteh.fivt.robot.RobotLeg;
import ru.fizteh.fivt.robot.RobotLegType;

import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyRobotLeg extends RobotLeg {
    private static boolean turn;
    private Integer step;
    private final Lock lock;

    public MyRobotLeg(RobotLegType type, OutputStream output, boolean myTurn, int stepCount, Lock locker) {
        super(type, output);
        turn = myTurn;
        step = stepCount;
        lock = locker;
    }

    public boolean step() {
        if (step == 0) {
            return false;
        }
        if (getType() == RobotLegType.RIGHT && !turn && lock.tryLock()) {
            try {
                makeStep();
                step--;
                turn = !turn;
                return true;
            } finally {
                lock.unlock();
            }
        }
        if (getType() == RobotLegType.LEFT && turn && lock.tryLock()) {
            try {
                makeStep();
                step--;
                turn = !turn;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return true;
    }
}
