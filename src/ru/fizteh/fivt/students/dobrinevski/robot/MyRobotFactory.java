package ru.fizteh.fivt.students.dobrinevski.robot;

import ru.fizteh.fivt.robot.*;

import java.io.OutputStream;

public class MyRobotFactory implements RobotFactory {

    public Robot createRobot(OutputStream output, int steps, RobotLegType firstStepLeg) {

        if (steps < 1) {
            throw new IllegalArgumentException("Irregular count of steps");
        }
        if (output == null) {
            throw new IllegalArgumentException("output is null");
        }
        if (firstStepLeg == null) {
            throw new IllegalArgumentException("third arg is null");
        }
        MyRobotLeg left;
        MyRobotLeg right;
        if (firstStepLeg == RobotLegType.LEFT) {
            left = new MyRobotLeg(RobotLegType.LEFT, output, true, (steps + 1) / 2);
            right = new MyRobotLeg(RobotLegType.RIGHT, output, true, steps / 2);
        } else {
            left = new MyRobotLeg(RobotLegType.LEFT, output, false, steps / 2);
            right = new MyRobotLeg(RobotLegType.RIGHT, output, false, (steps + 1) / 2);
        }

        return new Robot(left, right);
    }
}
