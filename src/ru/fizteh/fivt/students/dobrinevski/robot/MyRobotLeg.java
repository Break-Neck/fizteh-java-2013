package ru.fizteh.fivt.students.dobrinevski.robot;

import ru.fizteh.fivt.robot.RobotLeg;
import ru.fizteh.fivt.robot.RobotLegType;

import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyRobotLeg extends RobotLeg {
    private static boolean turn;
    private Integer step;
    private static final Lock LOCK = new ReentrantLock();

    public MyRobotLeg(RobotLegType type, OutputStream output, boolean myTurn, int stepCount) {
        super(type, output);
        turn = myTurn;
        step = stepCount;
    }

    public boolean step() {
        if (step == 0) {
            return false;
        }
        try {
            if (getType() == RobotLegType.RIGHT && !turn) {
                LOCK.lock();
                makeStep();
                step--;
                turn = !turn;
                return true;
            }
            if (getType() == RobotLegType.LEFT && turn) {
                LOCK.lock();
                makeStep();
                step--;
                turn = !turn;
                return true;
            }
            return true;
        } finally {
            LOCK.unlock();
        }
    }
}
