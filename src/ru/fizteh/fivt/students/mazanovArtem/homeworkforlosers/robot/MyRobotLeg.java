package ru.fizteh.fivt.students.mazanovArtem.homeworkforlosers.robot;

import ru.fizteh.fivt.robot.RobotLeg;
import ru.fizteh.fivt.robot.RobotLegType;

import java.io.OutputStream;

public class MyRobotLeg extends RobotLeg {
    public int step;
    MyRobot parent;

    public MyRobotLeg(RobotLegType type, OutputStream output, int steps, RobotLegType firstLegType) {
        super(type, output);
        if (steps < 0) {
            throw new IllegalArgumentException("steps is negative");
        }
        if (firstLegType.equals(type)) {
            step = (steps + 1) / 2;
        } else {
            step = steps - (steps + 1) / 2;
        }
    }

    public void setRobot(MyRobot robot) {
        parent = robot;
    }

    @Override
    public boolean step() {

        if (step > 0) {
            parent.lock.lock();
            try {
                if (getType() == parent.currentLeg) {
                    step--;
                    makeStep();
                    if (parent.currentLeg == RobotLegType.LEFT) {
                        parent.currentLeg = RobotLegType.RIGHT;
                    } else {
                        parent.currentLeg = RobotLegType.LEFT;
                    }
                }
                return true;
            } finally {
                parent.lock.unlock();
            }
        } else {
            return false;
        }
    }
}
