package ru.fizteh.fivt.students.dobrinevski.robot;

import ru.fizteh.fivt.robot.RobotLeg;
import ru.fizteh.fivt.robot.RobotLegType;

import java.io.OutputStream;

public class MyRobotLeg extends RobotLeg {
    protected MyRobotLeg(RobotLegType type, OutputStream output) {
        super(type, output);
    }

    public boolean step() {
        makeStep();
        return true;
    }
}
