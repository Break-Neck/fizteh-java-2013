package ru.fizteh.fivt.students.dobrinevski.robot;

import ru.fizteh.fivt.robot.RobotLeg;
import ru.fizteh.fivt.robot.RobotLegType;

import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyRobotLeg extends RobotLeg {
    private static boolean turn;
    private Integer step;
    private static final Lock LOCK = new ReentrantReadWriteLock().writeLock();;

    public MyRobotLeg(RobotLegType type, OutputStream output, boolean myTurn, int stepCount) {
        super(type, output);
        turn = myTurn;
        step = stepCount;
    }

    public boolean step() {
        if (step == 0) {
            return false;
        }
        LOCK.lock();
        try {
            if (getType() == RobotLegType.RIGHT && !turn) {
                makeStep();
                step--;
                turn = !turn;
                return true;
            }
            if (getType() == RobotLegType.LEFT && turn) {
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
