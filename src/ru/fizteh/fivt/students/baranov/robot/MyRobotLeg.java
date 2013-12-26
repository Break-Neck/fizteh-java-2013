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
        if (master.numberOfMadeSteps == master.getMaxNumberOfSteps()) {
            return false;
        }
        synchronized (master.numberOfMadeSteps) {
            if (master.numberOfMadeSteps % 2 == mode) {
                try {
                    master.numberOfMadeSteps.wait();
                } catch (InterruptedException e) {
                    //
                } catch (IllegalMonitorStateException e) {
                    //
                }
                master.numberOfMadeSteps++;
                makeStep();
            }
            try {
                master.numberOfMadeSteps.notify();
            } catch (IllegalMonitorStateException e) {
                //
            }
        }
        return true;
    }

    public void changeMode(int x) {
        this.mode = x;
    }
}
