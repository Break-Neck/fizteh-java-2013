package ru.fizteh.fivt.students.baranov.robot;

import ru.fizteh.fivt.robot.RobotLeg;
import ru.fizteh.fivt.robot.RobotLegType;

import java.io.OutputStream;

public class MyRobotLeg extends RobotLeg {
    public MyRobot master;
    private int mode;

    public MyRobotLeg(RobotLegType type, OutputStream output, MyRobot robot) {
        super(type, output);
        this.master = robot;
    }

    public boolean step() {
        synchronized (master.maxNumberOfSteps) {
            if (master.numberOfMadeSteps == master.maxNumberOfSteps) {
                return false;
            }
            if (master.numberOfMadeSteps % 2 == mode) {
                try {
                    master.maxNumberOfSteps.wait();
                } catch (InterruptedException e) {
                    //
                }
                master.numberOfMadeSteps++;
                makeStep();
            }
            master.maxNumberOfSteps.notify();
        }
        return true;
    }

    public void changeMode(int x) {
        this.mode = x;
    }
}
