package ru.fizteh.fivt.students.baranov.robot;

import ru.fizteh.fivt.robot.Robot;
import ru.fizteh.fivt.robot.RobotFactory;
import ru.fizteh.fivt.robot.RobotLegType;

import java.io.OutputStream;

public class MyRobotFactory implements RobotFactory {
    @Override
    public MyRobot createRobot(OutputStream output, int steps, RobotLegType firstStepLeg) {
        if (output == null) {
            throw new IllegalArgumentException("output is null");
        }
        if (steps < 0) {
            throw new IllegalArgumentException("steps should be positive number");
        }
        if (firstStepLeg != RobotLegType.LEFT && firstStepLeg != RobotLegType.RIGHT) {
            throw new IllegalArgumentException("wrong first step leg");
        }

        MyRobotLeg leftLeg = new MyRobotLeg(RobotLegType.LEFT, output, null);
        MyRobotLeg rightLeg = new MyRobotLeg(RobotLegType.RIGHT, output, null);

        if (firstStepLeg == RobotLegType.LEFT) {
            leftLeg.changeMode(0);
            rightLeg.changeMode(1);
        } else {
            leftLeg.changeMode(1);
            rightLeg.changeMode(0);
        }

        return new MyRobot(leftLeg, rightLeg, steps);
    }
}
